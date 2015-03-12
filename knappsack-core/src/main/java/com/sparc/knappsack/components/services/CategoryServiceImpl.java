package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.CategoryDao;
import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.forms.CategoryForm;
import com.sparc.knappsack.models.CategoryModel;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private static ApplicationContext ctx = null;

    @Qualifier("categoryDao")
    @Autowired(required = true)
    private CategoryDao categoryDao;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("appFileService")
    @Autowired
    private AppFileService appFileService;

    private static MultipartFile entertainmentIcon;
    private static MultipartFile productivityIcon;
    private static MultipartFile utilitiesIcon;

    @Override
    public List<Category> getAll() {
        return categoryDao.getAll();
    }

    @Override
    public Category get(Long id) {
        Category category = null;
        if (id != null && id > 0) {
            category = categoryDao.get(id);
        }

        return category;
    }

    private void save(Category category) {
        if (category != null) {
            if (category.getId() == null || category.getId() <= 0) {
                categoryDao.add(category);
            } else {
                categoryDao.update(category);
            }
        }
    }

    @Override
    public void add(Category category) {
        categoryDao.add(category);
    }

    @Override
    public void delete(Long id) {
        delete(get(id));
    }

    @Override
    public void delete(Category category) {
        if (category != null) {
            deleteIcon(category);
            Organization organization = category.getOrganization();
            organization.getCategories().remove(category);

            categoryDao.delete(category);
        }
    }

    @Override
    public void update(Category category) {
        categoryDao.update(category);
    }

    @Override
    public Category updateCategory(CategoryForm categoryForm) {

        Category savedCategory = get(categoryForm.getId());
        mapCategoryFields(categoryForm, savedCategory);
//        update(savedCategory);

        return savedCategory;
    }

    @Override
    public Category saveCategory(CategoryForm categoryForm) {
        Category savedCategory = new Category();
        mapCategoryFields(categoryForm, savedCategory);
//        add(savedCategory);

        return savedCategory;
    }

    private void mapCategoryFields(CategoryForm categoryForm, Category category) {
        category.setDescription(categoryForm.getDescription());
        category.setName(categoryForm.getName());

        Organization organization = organizationService.get(categoryForm.getOrganizationId());
        category.setOrganization(organization);
        if (!organization.getCategories().contains(category)) {
            organization.getCategories().add(category);
        }

        save(category);

        Long storageConfigurationId = categoryForm.getStorageConfigurationId();
        Long orgStorageConfigId = categoryForm.getOrgStorageConfigId();
        //If null we are editing a category
        if (storageConfigurationId == null) {
            storageConfigurationId = category.getStorageConfiguration().getId();
        }

        category.setStorageConfiguration(storageConfigurationService.get(storageConfigurationId));
        AppFile icon = createIcon(categoryForm.getIcon(), orgStorageConfigId, storageConfigurationId, category.getUuid());
        if (icon != null) {
            icon.setStorable(category);
            category.setIcon(icon);
        }

        save(category);
    }

    private AppFile createIcon(MultipartFile icon, Long orgStorageConfigId, Long storageConfigurationId, String uuid) {
        StorageService storageService = getStorageService(storageConfigurationId);
        return storageService.save(icon, AppFileType.ICON.getPathName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    @Override
    public void deleteIcon(Long categoryId) {
        deleteIcon(get(categoryId));
    }

    @Override
    public void deleteIcon(Category category) {
        if (category != null) {
            AppFile appFile = category.getIcon();
            category.setIcon(null);
            appFileService.delete(appFile);
        }
    }

    @Override
    public CategoryModel createCategoryModel(Category category, boolean includeIcon) {
        CategoryModel categoryModel = null;
        if (category != null) {
            categoryModel = new CategoryModel();
            categoryModel.setId(category.getId());
            categoryModel.setName(category.getName());
            categoryModel.setDescription(category.getDescription());
            if (includeIcon) {
                categoryModel.setIcon(appFileService.createImageModel(category.getIcon()));
            }
        }

        return categoryModel;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    public List<Category> createDefaultCategories(Long organizationId) {
        List<Category> defaultCategories = new ArrayList<Category>();
        Organization organization = organizationService.get(organizationId);
        StorageConfiguration storageConfiguration = organization.getOrgStorageConfig().getStorageConfigurations().get(0);
        Long storageConfigId = storageConfiguration.getId();
        Long orgStorageConfigId = organization.getOrgStorageConfig().getId();

        /**
         * Create entertainment category
         */
        Category entertainmentCategory = new Category();
        entertainmentCategory.setName("Entertainment");
        entertainmentCategory.setDescription("Applications featuring a bit of levity.");
        entertainmentCategory.setStorageConfiguration(storageConfiguration);
        entertainmentCategory.setOrganization(organization);
        organization.getCategories().add(entertainmentCategory);
        add(entertainmentCategory);

        AppFile icon = createIcon(entertainmentIcon, orgStorageConfigId, storageConfigId, entertainmentCategory.getUuid());
        if (icon != null) {
            icon.setStorable(entertainmentCategory);
            entertainmentCategory.setIcon(icon);
        }
        defaultCategories.add(entertainmentCategory);

        /**
         * Create productivity category
         */
        Category productivityCategory = new Category();
        productivityCategory.setName("Productivity");
        productivityCategory.setDescription("Applications to make your day to day more efficient.");
        productivityCategory.setStorageConfiguration(storageConfiguration);
        productivityCategory.setOrganization(organization);
        organization.getCategories().add(productivityCategory);
        add(productivityCategory);

        icon = createIcon(productivityIcon, orgStorageConfigId, storageConfigId, productivityCategory.getUuid());
        if (icon != null) {
            icon.setStorable(productivityCategory);
            productivityCategory.setIcon(icon);
        }
        defaultCategories.add(productivityCategory);

        /**
         * Create utilities category
         */
        Category utilitiesCategory = new Category();
        utilitiesCategory.setName("Utilities");
        utilitiesCategory.setDescription("Applications with a specific skill set.");
        utilitiesCategory.setStorageConfiguration(storageConfiguration);
        utilitiesCategory.setOrganization(organization);
        organization.getCategories().add(utilitiesCategory);
        add(utilitiesCategory);

        icon = createIcon(utilitiesIcon, orgStorageConfigId, storageConfigId, utilitiesCategory.getUuid());
        if (icon != null) {
            icon.setStorable(utilitiesCategory);
            utilitiesCategory.setIcon(icon);
        }
        defaultCategories.add(utilitiesCategory);

        return defaultCategories;
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
        initIcons();
    }

    private static void initIcons() {
        try {
            InputStream inputStream = ctx.getResource("resources/img/icon_entertainment.png").getInputStream();
            entertainmentIcon = new MockMultipartFile("defaultCategory1", "icon_entertainment.png", "image/png", IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            log.warn("Error loading entertainment icon for default categories", e);
        }

        try {
            InputStream inputStream = ctx.getResource("resources/img/icon_productivity.png").getInputStream();
            productivityIcon = new MockMultipartFile("defaultCategory2", "icon_productivity.png", "image/png", IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            log.warn("Error loading productivity icon for default categories", e);
        }

        try {
            InputStream inputStream = ctx.getResource("resources/img/icon_utilities.png").getInputStream();
            utilitiesIcon = new MockMultipartFile("defaultCategory3", "icon_utilities.png", "image/png", IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            log.warn("Error loading utilities icon for default categories", e);
        }
    }
}
