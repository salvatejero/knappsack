package com.sparc.knappsack.components.controllers;

import com.sparc.knappsack.components.entities.DropBoxStorageConfiguration;
import com.sparc.knappsack.components.entities.S3StorageConfiguration;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import com.sparc.knappsack.components.services.StorageConfigurationService;
import com.sparc.knappsack.components.validators.StorageConfigurationValidator;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.StorageForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
public class StorageController extends AbstractController {

    private static final String MANAGER_STORAGE = "manager/storageConfigsTH";

    @Qualifier("storageConfigurationService")
    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Qualifier("storageConfigurationValidator")
    @Autowired
    private StorageConfigurationValidator storageConfigurationValidator;

    @InitBinder("storageForm")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(storageConfigurationValidator);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/viewStorageConfigs", method = RequestMethod.GET)
    public String viewStorageConfigs(Model model) {
        if(!model.containsAttribute("storageForm")) {
            model.addAttribute("storageForm", new StorageForm());
            model.addAttribute("storageType", StorageType.LOCAL.name());
        }
        model.addAttribute("storageConfigurations", storageConfigurationService.getAll());
        model.addAttribute("storageTypes", StorageType.values());

        return MANAGER_STORAGE;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/saveStorageConfiguration", method = RequestMethod.POST)
    public String saveStorageConfiguration(Model model, @ModelAttribute("storageForm") @Validated StorageForm storageForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            if (storageForm.isEditing()) {
                return editStorageConfiguration(model, storageForm.getId());
            } else {
                return addStorageConfig(model);
            }
        }

        if(storageForm.getId() != null && storageForm.getId() > 0) {
            storageConfigurationService.update(storageForm);
        } else {
            storageConfigurationService.createStorageConfiguration(storageForm);
        }
        model.addAttribute("storageForm", new StorageForm());
        model.addAttribute("storageType", StorageType.LOCAL.name());
        return "redirect:/manager/viewStorageConfigs";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/deleteStorageConfiguration/{id}", method = RequestMethod.GET)
    public String deleteStorageConfiguration(@PathVariable Long id) {
        checkRequiredEntity(storageConfigurationService, id);

        storageConfigurationService.delete(id);

        return "redirect:/manager/viewStorageConfigs";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/addStorageConfig", method = RequestMethod.GET)
    public String addStorageConfig(Model model) {

        if(!model.containsAttribute("storageForm")) {
            model.addAttribute("storageForm", new StorageForm());
        }

        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("isEdit", false);

        return "manager/manageStorageConfigTH";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/manager/editStorageConfiguration/{id}", method = RequestMethod.GET)
    public String editStorageConfiguration(Model model, @PathVariable Long id) {
        checkRequiredEntity(storageConfigurationService, id);

        StorageConfiguration storageConfiguration = storageConfigurationService.get(id);

        if (storageConfiguration != null) {
            if (!model.containsAttribute("storageForm")) {
                StorageForm storageForm = new StorageForm();
                storageForm.setId(storageConfiguration.getId());
                storageForm.setBaseLocation(storageConfiguration.getBaseLocation());
                storageForm.setName(storageConfiguration.getName());
                storageForm.setStorageType(storageConfiguration.getStorageType());
                storageForm.setRegistrationDefault(storageConfiguration.isRegistrationDefault());
                storageForm.setEditing(true);

                if(storageConfiguration instanceof S3StorageConfiguration) {
                    storageForm.setAccessKey(((S3StorageConfiguration) storageConfiguration).getAccessKey());
                    storageForm.setSecretKey(((S3StorageConfiguration) storageConfiguration).getSecretKey());
                    storageForm.setBucketName(((S3StorageConfiguration) storageConfiguration).getBucketName());
                }if(storageConfiguration instanceof DropBoxStorageConfiguration) {
                    storageForm.setAccessKey(((DropBoxStorageConfiguration) storageConfiguration).getAccessKey());
                    storageForm.setSecretKey(((DropBoxStorageConfiguration) storageConfiguration).getSecretKey());
                    storageForm.setBucketName(((DropBoxStorageConfiguration) storageConfiguration).getBucketName());
                }


                model.addAttribute("storageForm", storageForm);
            } else {
                ((StorageForm) model.asMap().get("storageForm")).setStorageType(storageConfiguration.getStorageType());
            }
            model.addAttribute("originalName", storageConfiguration.getName());
        }

        model.addAttribute("storageTypes", StorageType.values());
        model.addAttribute("isEdit", true);

        return "manager/manageStorageConfigTH";
    }

}
