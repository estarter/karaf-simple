package org.maven.example;

import java.io.File;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

/**
 * @author Alexey Merezhin
 */
public class Simple implements SimpleI {
    private static final Logger LOG = LoggerFactory.getLogger(Simple.class);

    final private String sysPath;
    final private String myProp;

    public Simple(String sysPath, String myProp) {
        this.sysPath = sysPath;
        this.myProp = myProp;
        LOG.info("init {}", toString());
    }

    @Override
    public String toString() {
        return String.format("Simple with path '%s' and prop '%s'", sysPath, myProp);
    }

    public void doAction() throws Docx4JException, Xlsx4jException {
        // Open a document from the file system
        SpreadsheetMLPackage xlsxPkg = SpreadsheetMLPackage.load(new File("/tmp/file-orig.xlsx"));

        WorkbookPart workbookPart = xlsxPkg.getWorkbookPart();

        WorksheetPart sheet = workbookPart.getWorksheet(0);

        // Now lets print the cell content
        doc4j_processContent(sheet,workbookPart);

        // save data
        xlsxPkg.save(new File("/tmp/file.xlsx"));
    }

    private void doc4j_processContent(WorksheetPart sheet,
            WorkbookPart workbookPart) throws Docx4JException {
        Worksheet ws = sheet.getContents();

        SheetData data = ws.getSheetData();

        for (Row r : data.getRow()) {
            for (Cell c : r.getC()) {
                if (c.getR().equals("A1") || c.getR().startsWith("B") || c.getR().equals("F2")) {
                    CellReference celRef = new CellReference(c, workbookPart.getSharedStrings());
                    System.out.println("CELL[" + c.getR() + "] value = " + celRef.getStringCellValue());
                    celRef.setCellValue("REPLACED");
                }
            }
        }

    }

}
