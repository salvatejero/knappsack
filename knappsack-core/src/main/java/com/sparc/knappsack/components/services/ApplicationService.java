package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.ApplicationForm;
import com.sparc.knappsack.models.ApplicationModel;

import java.util.List;

public interface ApplicationService extends EntityService<Application> {
    /**
     * @return List<Application> - return all Applications in the system
     */
    List<Application> getAll();

    /**
     * @param applicationType ApplicationType
     * @return List<Application> - return all Applications of a given application type
     */
    List<Application> getAll(ApplicationType applicationType);

    /**
     * @param application Application - the Application.  This will only delete the icons, screens hots, installation files and application versions.  It will not delete the Application itself.
     */
    void deleteApplicationFilesAndVersions(Application application);

    /**
     * @param searchCriteria String - Name and description of the Application
     * @return List<Application> - return all Applications meeting this criteria
     */
    List<Application> getAllByNameAndDescription(String searchCriteria);

    List<Application> getAll(Group group, ApplicationType... applicationTypes);

    /**
     * @param category Category
     * @return List<Application> - return all Applications belonging to the given Category
     */
    List<Application> getAll(Category category);

    /**
     * @param category Category
     * @param applicationType ApplicationType
     * @return List<ApplicationModel> - return ApplicationModel objects based on the Category and ApplicationType
     */
    List<ApplicationModel> getAll(Category category, ApplicationType applicationType);

    /**
     * @param applications List<Application>
     * @return List<ApplicationModel> - return a list of ApplicationModel objects populated with data from the specified Applications
     */
    List<ApplicationModel> createApplicationModels(List<Application> applications, boolean mapScreenShots);

    /**
     * @param applicationId Long
     * @return ApplicationModel - return an ApplicationModel populated with data from an Application
     */
    ApplicationModel createApplicationModel(Long applicationId, boolean mapScreenShots);

    <D> D getApplicationModel(Long applicationId, Class<D> modelClass);

    <D> List<D> getApplicationModels(List<Application> applications, Class<D> modelClass);

    /**
     * @param application Application
     * @return ApplicationModel - return an ApplicationModel populated with data from an Application
     */
    ApplicationModel createApplicationModel(Application application, boolean mapScreenShots);

    /**
     * @param applicationForm UploadApplication
     * @return Application - create and persist and Application given the data in the UploadApplication
     */
    Application saveApplication(ApplicationForm applicationForm);

    /**
     * @param application Application
     * @param applicationType ApplicationType
     * @return boolean - return true if the device's ApplicationType matches that of the Application
     */
    boolean determineApplicationVisibility(Application application, ApplicationType applicationType);

    /**
     * @param applicationId Long
     * @param index int
     */
    void deleteScreenshot(Long applicationId, int index);

    /**
     * @param applicationId Long - deletes the Application's icon from the system
     */
    void deleteIcon(Long applicationId);

    /**
     * @param application Application - Check how many versions currently exist for this Application
     * @param domain Domain - Check how many application versions are allowed for this Domain.
     * @return boolean true if the amount of versions allowed for this application and domain has reached its maximum.
     */
    boolean isApplicationVersionLimit(Application application, Domain domain);

    /**
     * @return Count of all Applications
     */
    long countAll();
}
