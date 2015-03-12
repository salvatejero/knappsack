package com.sparc.knappsack.components.services;

import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service("excelService")
public class ExcelServiceImpl implements ExcelService {

    private static final Logger log = LoggerFactory.getLogger(ExcelServiceImpl.class);
    private static final String NOT_AVAILABLE_VALUE = "N/A";

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Override
    public void createOrganizationDetailsForDateRangeReport(Date minDate, Date maxDate, OutputStream outputStream) {
        if (outputStream == null) {
            log.error("Attempted to create Organization Details For Date Range report without an OutputStream");
            return;
        }
        List<OrganizationModel> organizationModels = organizationService.getAllOrganizationsForCreateDateRange(minDate, maxDate);

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet worksheet = workbook.createSheet("Organization Details");
        int startRowIndex = 0;
        int startColIndex = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        buildTitle(worksheet, String.format("Organization Details for Date Range: %s - %s", (minDate == null ? "Start" : sdf.format(minDate)), (maxDate == null ? "Present" : sdf.format(maxDate))), startRowIndex, startColIndex);
        buildHeaders(worksheet, startRowIndex, startColIndex, new String[]{"NAME", "Ceate Date", "Payment Plan", "Customer Id", "Status", "Admins"});

        HSSFCreationHelper helper = workbook.getCreationHelper();

        startRowIndex += 2;
        HSSFCellStyle bodyCellStyle = worksheet.getWorkbook().createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
        bodyCellStyle.setWrapText(false);
        bodyCellStyle.setDataFormat(helper.createDataFormat().getFormat("m/d/yyyy"));

        HSSFCellStyle hlink_style = workbook.createCellStyle();
        Font hlink_font = workbook.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);

        int previousRowNum = 2;
        int previousRowSize = 1;
        for (int i = 0; i < organizationModels.size(); i++) {
            HSSFRow row = worksheet.createRow(previousRowNum + previousRowSize);
            HSSFCell cell1 = row.createCell(startColIndex + 0);
            cell1.setCellValue(organizationModels.get(i).getName());
            cell1.setCellStyle(bodyCellStyle);

            HSSFCell cell2 = row.createCell(startColIndex+1);
            cell2.setCellValue(organizationModels.get(i).getCreateDate());
            cell2.setCellStyle(bodyCellStyle);

            HSSFCell cell3 = row.createCell(startColIndex+2);

            cell3.setCellValue(NOT_AVAILABLE_VALUE);
            cell3.setCellStyle(bodyCellStyle);

            HSSFCell cell4 = row.createCell(startColIndex+3);

            cell4.setCellValue(NOT_AVAILABLE_VALUE);
            cell4.setCellStyle(bodyCellStyle);

            HSSFCell cell5 = row.createCell(startColIndex+4);

            cell5.setCellValue(NOT_AVAILABLE_VALUE);
            cell5.setCellStyle(bodyCellStyle);

            boolean createRow = false;
            List<UserDomainModel> userDomainModels = organizationService.getAllOrganizationAdmins(organizationModels.get(i).getId());

            for (int x = 0; x < userDomainModels.size(); x++) {
                String name = userDomainModels.get(x).getUser().getFullName();
                String email = userDomainModels.get(x).getUser().getEmail();
                HSSFHyperlink link = helper.createHyperlink(Hyperlink.LINK_EMAIL);

                HSSFCell cell;
                if (!createRow) {
                    cell = row.createCell(startColIndex+5);
                    createRow = true;
                } else {
                    HSSFRow newRow = worksheet.createRow(previousRowNum + x + 1);
                    cell = newRow.createCell(startColIndex+5);
                }

                cell.setCellValue(name);
                link.setAddress("mailto:" + email);
                cell.setHyperlink(link);
                cell.setCellStyle(hlink_style);

            }
            previousRowNum = row.getRowNum();
            previousRowSize = (userDomainModels.size() > 0 ? userDomainModels.size() : 1);
        }

        worksheet.autoSizeColumn(0);
        worksheet.autoSizeColumn(1);
        worksheet.autoSizeColumn(2);
        worksheet.autoSizeColumn(3);
        worksheet.autoSizeColumn(4);
        worksheet.autoSizeColumn(5);

        try {
            worksheet.getWorkbook().write(outputStream);
        } catch (IOException e) {
            log.error("Error while writing Organization Details For Date Range report to OutputStream.", e);
        }
    }

    public void buildTitle(HSSFSheet worksheet, String title, int startRowIndex, int startColIndex) {
        // Create font style for the report title
        Font fontTitle = worksheet.getWorkbook().createFont();
        fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontTitle.setFontHeight((short) 280);

        // Create cell style for the report title
        HSSFCellStyle cellStyleTitle = worksheet.getWorkbook().createCellStyle();
        cellStyleTitle.setAlignment(CellStyle.ALIGN_CENTER);
        cellStyleTitle.setWrapText(false);
        cellStyleTitle.setFont(fontTitle);

        // Create report title
        HSSFRow rowTitle = worksheet.createRow((short) startRowIndex);
        rowTitle.setHeight((short) 500);
        HSSFCell cellTitle = rowTitle.createCell(startColIndex);
        cellTitle.setCellValue(title);
        cellTitle.setCellStyle(cellStyleTitle);

        // Create merged region for the report title
        worksheet.addMergedRegion(new CellRangeAddress(0,0,0,4));

        // Create date header
        HSSFRow dateTitle = worksheet.createRow((short) startRowIndex +1);
        HSSFCell cellDate = dateTitle.createCell(startColIndex);
        cellDate.setCellValue("This report was generated at " + new Date());

        // Create merged region for the report title
        worksheet.addMergedRegion(new CellRangeAddress(1,1,0,4));
    }

    public static void buildHeaders(HSSFSheet worksheet, int startRowIndex, int startColIndex, String[] headerNames) {
        // Create font style for the headers
        Font font = worksheet.getWorkbook().createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);

        // Create cell style for the headers
        HSSFCellStyle headerCellStyle = worksheet.getWorkbook().createCellStyle();
        headerCellStyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(CellStyle.FINE_DOTS);
        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headerCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        headerCellStyle.setWrapText(false);
        headerCellStyle.setFont(font);
        headerCellStyle.setBorderBottom(CellStyle.BORDER_THIN);

        // Create the column headers
        HSSFRow rowHeader = worksheet.createRow((short) startRowIndex +2);
        rowHeader.setHeight((short) 500);

        for (int x = 0; x < headerNames.length; x++) {
            HSSFCell cell = rowHeader.createCell(startColIndex + x);
            cell.setCellValue(headerNames[x]);
            cell.setCellStyle(headerCellStyle);
        }
    }
}
