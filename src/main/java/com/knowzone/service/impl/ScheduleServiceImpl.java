package com.knowzone.service.impl;

import com.knowzone.dto.GroupTopicCombination;
import com.knowzone.enums.GroupCreationType;
import com.knowzone.persistence.entity.Group;
import com.knowzone.persistence.entity.Region;
import com.knowzone.persistence.repository.GroupRepository;
import com.knowzone.persistence.repository.RegionRepository;
import com.knowzone.persistence.repository.UserRepository;
import com.knowzone.service.ScheduleService;
import com.knowzone.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final GroupRepository groupRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final TopicService topicService;


    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Her gün gece yarısı (00:00)
    public void performDailyTasks() {
        try {
            log.info("Starting random group creation from unused regions...");

            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<Long> usedRegionIds = groupRepository.findRegionIdsOfGroupsCreatedInLastDays(threeDaysAgo);

            log.info("Found {} regions used in the last 3 days", usedRegionIds.size());

            List<Region> allRegions = regionRepository.findByIsActiveTrue();

            List<Region> unusedRegions = allRegions.stream()
                    .filter(region -> !usedRegionIds.contains(region.getId()))
                    .toList();

            log.info("Found {} unused regions out of {} total regions", unusedRegions.size(), allRegions.size());

            if (unusedRegions.isEmpty()) {
                log.info("No unused regions found, skipping group creation");
                return;
            }

            List<Region> shuffledRegions = new ArrayList<>(unusedRegions);
            Collections.shuffle(shuffledRegions);

            int randomCount = random.nextInt(shuffledRegions.size() + 1);

            log.info("Will create {} random groups from {} unused regions", randomCount, shuffledRegions.size());

            for (int i = 0; i < randomCount && i < shuffledRegions.size(); i++) {
                Region region = shuffledRegions.get(i);
                LocalTime startTime = LocalTime.of(random.nextInt(12, 23), 0);
                createRandomGroup(region, GroupCreationType.DAILY_SCHEDULE,startTime);
            }

            log.info("Successfully created {} random groups", randomCount);

        } catch (Exception e) {
            log.error("Error during random group creation: ", e);
        }
    }

    @Override
    @Scheduled(cron = "0 */30 * * * ?")  // Her yarım saatte bir
    public void performHourlyTasks() {
        try {
            log.info("Starting emergency group creation for high density regions...");

            double radiusKm = 2.0;

            List<Region> activeRegions = regionRepository.findByIsActiveTrue();

            if (activeRegions.isEmpty()) {
                log.info("No active regions found");
                return;
            }

            log.info("Analyzing {} active regions for high density areas...", activeRegions.size());

            int groupsCreated = 0;

            for (int i = 0; i < activeRegions.size(); i++) {

                Region region = activeRegions.get(i);

                if (region.getLatitude() == null || region.getLongitude() == null) {
                    log.warn("Region {} (index: {}) has no coordinates, skipping", region.getName(), i);
                    continue;
                }

                log.info("Processing region {} (index: {}) - {} ({}, {})", i, region.getName(), region.getCity(), region.getCountry());

                Long userCount = userRepository.countUsersInRegionRadius(
                        region.getLatitude(),
                        region.getLongitude(),
                        radiusKm
                );

                log.info("Region {} (index: {}) has {} users within {}km radius",
                        region.getName(), i, userCount, radiusKm);

                if (userCount >= 10) {
                    LocalTime startTime = LocalTime.now().plusMinutes(10);
                    createRandomGroup(region,GroupCreationType.DEMAND_BASED,startTime);
                }
            }
        }
        catch (Exception e) {
            log.error("Error during group creation: ", e);
        }

    }

    private void createRandomGroup(Region region, GroupCreationType groupCreationType, LocalTime startTime) {
        try {
            GroupTopicCombination groupTopicCombination = topicService.getRandomGroupTopic();
            String randomName = groupTopicCombination.getName();
            String randomDescription = groupTopicCombination.getDescription();
            int maxMembers = random.nextInt(4, 10);
            LocalTime endTime = LocalTime.of(startTime.getHour() + random.nextInt(1, 4), 0); // 1-3 saat süre

            Group group = Group.builder()
                    .name(randomName)
                    .description(randomDescription)
                    .region(region)
                    .maxMembers(maxMembers)
                    .currentMembers(0)
                    .startTime(startTime)
                    .endTime(endTime)
                    .isActive(true)
                    .groupCreationType(groupCreationType)
                    .build();

            Group savedGroup = groupRepository.save(group);
            log.info("Created random group: {} in region: {} (ID: {})",
                    randomName, region.getName(), savedGroup.getId());

        } catch (Exception e) {
            log.error("Error creating random group for region {}: ", region.getName(), e);
        }
    }
}
