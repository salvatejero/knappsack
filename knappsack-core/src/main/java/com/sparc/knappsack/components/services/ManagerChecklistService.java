package com.sparc.knappsack.components.services;

import com.sparc.knappsack.models.ManagerChecklist;

public interface ManagerChecklistService {

    /**
     * @param organizationId Long - Organization for which to generate a checklist of pending critical actions
     * @return ManagerChecklist - a list of critical actions and information pending or completed for this organization
     */
    ManagerChecklist getManagerChecklist(Long organizationId);
}
