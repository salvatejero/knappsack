package com.sparc.knappsack.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ApplicationType {
    ANDROID(1, "applicationType.android", true, MimeType.ANDROID, false, 1, 1),
    IPHONE(2, "applicationType.iphone", true, MimeType.IOS, false, 2, 4),
    IPAD(3, "applicationType.ipad", true, MimeType.IOS, false, 2, 4),
    IOS(4, "applicationType.ios", true, MimeType.IOS, true, 2, 4),
    CHROME(5, "applicationType.chrome", false, MimeType.CHROME, false, 3, 5),
    FIREFOX(6, "applicationType.firefox", false, MimeType.FIREFOX, false, 4, 6),
    WINDOWSPHONE7(7, "applicationType.windowsphone7", true, MimeType.WINPHONE, false, 5, 7),
    BLACKBERRY(8, "applicationType.blackberry", true, MimeType.BLACKBERRY_COD, false, 6, 8),
    OTHER(9, "applicationType.other", false, null, false, 7, 9);
;
    private final long id;
    private final String messageKey;
    private final boolean isMobilePlatform;
    private final MimeType mimeType;
    private final boolean keyVaultCandidate;
    private final int groupId;
    private final long parentId;

    ApplicationType(long id, String messageKey, boolean isMobilePlatform, MimeType mimeType, boolean keyVaultCandidate, int groupId, long parentId) {
        this.id = id;
        this.messageKey = messageKey;
        this.isMobilePlatform = isMobilePlatform;
        this.mimeType = mimeType;
        this.keyVaultCandidate = keyVaultCandidate;
        this.groupId = groupId;
        this.parentId = parentId;
    }

    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getMessageKey() {
        return messageKey;
    }

    public boolean isMobilePlatform() {
        return isMobilePlatform;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public boolean isKeyVaultCandidate() {
        return keyVaultCandidate;
    }

    public int getGroupId() {
        return groupId;
    }

    public long getParentId() {
        return parentId;
    }

    public static List<ApplicationType> getAllKeyVaultCandidates() {
        List<ApplicationType> candidates = new ArrayList<ApplicationType>();

        for (ApplicationType applicationType : ApplicationType.values()) {
            if (applicationType.isKeyVaultCandidate()) {
                candidates.add(applicationType);
            }
        }

        return candidates;
    }

    public static List<ApplicationType> getAllInGroup(ApplicationType applicationType) {
        List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
        if (applicationType != null) {
            for (ApplicationType type : ApplicationType.values()) {
                if (type.groupId == applicationType.groupId) {
                    applicationTypes.add(type);
                }
            }
        }
        return applicationTypes;
    }

    public static ApplicationType getById(long id) {
        for (ApplicationType applicationType : values()) {
            if (applicationType.id == id) {
                return applicationType;
            }
        }

        return null;
    }

    public static List<ApplicationType> getAllForUserDeviceType(ApplicationType deviceType) {
        List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
        if (deviceType != null) {
            if (!deviceType.isMobilePlatform) {
                // Not mobile so add all
                applicationTypes.addAll(Arrays.asList(values()));
            } else {
                applicationTypes.add(deviceType);
                applicationTypes.add(ApplicationType.getById(deviceType.getParentId()));
                applicationTypes.addAll(getAllChildrenDeviceTypes(deviceType));
            }
        }

        return applicationTypes;
    }

    public static List<ApplicationType> getAllChildrenDeviceTypes(ApplicationType deviceType) {
        List<ApplicationType> children = new ArrayList<ApplicationType>();
        if (deviceType != null) {
            for (ApplicationType applicationType : values()) {
                if (deviceType.id == applicationType.parentId) {
                    children.add(applicationType);
                }
            }
        }
        return children;
    }
}
