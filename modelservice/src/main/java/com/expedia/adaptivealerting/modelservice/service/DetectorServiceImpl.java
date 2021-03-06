/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expedia.adaptivealerting.modelservice.service;

import com.expedia.adaptivealerting.modelservice.entity.Detector;
import com.expedia.adaptivealerting.modelservice.entity.Detector.DetectorConfig;
import com.expedia.adaptivealerting.modelservice.entity.Detector.TrainingMetaData;
import com.expedia.adaptivealerting.modelservice.exception.RecordNotFoundException;
import com.expedia.adaptivealerting.modelservice.repo.DetectorRepository;
import com.expedia.adaptivealerting.modelservice.util.DateUtil;
import com.expedia.adaptivealerting.modelservice.util.DetectorDataUtil;
import com.expedia.adaptivealerting.modelservice.util.RequestValidator;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.isNull;
import static com.expedia.adaptivealerting.anomdetect.util.AssertUtil.notNull;

@Service
public class DetectorServiceImpl implements DetectorService {

    @Autowired
    private DetectorRepository repository;

    @Override
    public UUID createDetector(Detector detector) {
        notNull(detector, "detectorDto can't be null");
        isNull(detector.getUuid(), "Required: detectorDto.uuid == null");

        val uuid = UUID.randomUUID();
        detector.setId(uuid.toString());
        detector.setUuid(uuid);
        detector.setMeta(DetectorDataUtil.buildNewDetectorMeta(detector));
        RequestValidator.validateDetector(detector);
        repository.save(detector);
        return uuid;
    }

    @Override
    public Detector findByUuid(String uuid) {
        Detector detector = repository.findByUuid(uuid);
        if (detector == null) {
            throw new RecordNotFoundException("Invalid UUID: " + uuid);
        }
        return detector;
    }

    @Override
    public List<Detector> findByCreatedBy(String user) {
        List<Detector> detectors = repository.findByMeta_CreatedBy(user);
        if (detectors == null || detectors.isEmpty()) {
            throw new RecordNotFoundException("Invalid user: " + user);
        }
        return detectors;
    }

    @Override
    public void toggleDetector(String uuid, Boolean enabled) {
        Detector detector = repository.findByUuid(uuid);
        detector.setEnabled(enabled);
        repository.save(detector);
    }

    @Override
    public void trustDetector(String uuid, Boolean trusted) {
        Detector detector = repository.findByUuid(uuid);
        detector.setTrusted(trusted);
        repository.save(detector);
    }

    @Override
    public List<Detector> getLastUpdatedDetectors(long interval) {
        val now = DateUtil.now().toInstant();
        val fromDate = DateUtil.toUtcDateString((now.minus(interval, ChronoUnit.SECONDS)));
        return repository.findByMeta_DateLastUpdatedGreaterThan(fromDate);
    }

    @Override
    public List<Detector> getLastUsedDetectors(int noOfDays) {
        val now = DateUtil.now().toInstant();
        val fromDate = DateUtil.toUtcDateString((now.minus(noOfDays, ChronoUnit.DAYS)));
        return repository.findByMeta_DateLastAccessedLessThan(fromDate);
    }

    @Override
    public List<Detector> getDetectorsToBeTrained(long timestampMs) {
        val trainInstant = Instant.ofEpochMilli(timestampMs);
        val date = DateUtil.toUtcDateString(trainInstant);
        return repository.findByDetectorConfig_TrainingMetaData_DateTrainingNextRunLessThan(date);
    }

    @Override
    public void updateDetector(String uuid, Detector detector) {
        notNull(detector, "detector can't be null");
        MDC.put("DetectorUuid", uuid);

        Detector detectorToBeUpdated = repository.findByUuid(uuid);
        DetectorConfig detectorConfigToUpdate = DetectorDataUtil.buildMergedDetectorConfig(
            detectorToBeUpdated.getDetectorConfig(),
            Optional.ofNullable(detector.getDetectorConfig()));
        detectorToBeUpdated.setDetectorConfig(detectorConfigToUpdate);
        detectorToBeUpdated.setMeta(DetectorDataUtil.buildLastUpdatedDetectorMeta(detector));
        RequestValidator.validateDetector(detectorToBeUpdated);
        repository.save(detectorToBeUpdated);
    }

    @Override
    public void updateDetectorLastUsed(String uuid) {
        notNull(uuid, "uuid can't be null");
        MDC.put("DetectorUuid", uuid);

        Detector detectorToBeUpdated = repository.findByUuid(uuid);
        detectorToBeUpdated.setMeta(DetectorDataUtil.buildLastUsedDetectorMeta(detectorToBeUpdated));
        RequestValidator.validateDetector(detectorToBeUpdated);
        repository.save(detectorToBeUpdated);
    }

    @Override
    public void updateDetectorTrainingTime(String uuid, long nextRun) {
        notNull(uuid, "uuid can't be null");
        MDC.put("DetectorUuid", uuid);
        Detector detectorToBeUpdated = repository.findByUuid(uuid);
        TrainingMetaData updatedTrainingTimeMeta = DetectorDataUtil.buildUpdatedRuntimeTrainingMeta(
            detectorToBeUpdated, nextRun);
        detectorToBeUpdated.getDetectorConfig().setTrainingMetaData(updatedTrainingTimeMeta);
        repository.save(detectorToBeUpdated);
    }

    @Override
    public void deleteDetector(String uuid) {
        repository.deleteByUuid(uuid);
    }

}
