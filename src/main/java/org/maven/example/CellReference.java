package org.maven.example;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.STCellType;

/**
 * @author e3ckuo
 */
public class CellReference {
    /* The character ($) that signifies a row or column value is absolute instead of relative */
    private static final char ABSOLUTE_REFERENCE_MARKER = '$';

    /* Matches a run of one or more letters followed by a run of one or more digits. The run of letters is group 1
      and the run of digits is group 2. Each group may optionally be prefixed with a single '$'. */
    private static final Pattern CELL_REF_PATTERN = Pattern.compile("\\$?([A-Za-z]+)\\$?([0-9]+)");

    private final Cell cell;
    private final SharedStrings sharedStrings;
    private String columnString;
    private Integer columnIndex;
    private Integer rowIndex;

    public CellReference(Cell cell, SharedStrings sharedStrings) {
        this.cell = cell;
        this.sharedStrings = sharedStrings;
        columnString = null;
        columnIndex = null;
        rowIndex = null;
    }

    public String getStringCellValue() {
        try {
            STCellType type = cell.getT();
            switch (type) {
                case STR: // formula
                    return cell.getV();
                case INLINE_STR: // inline (rich) string
                    CTRst is = cell.getIs();
                    if (is != null) {
                        CTXstringWhitespace t = is.getT();
                        if (t != null) {
                            return t.getValue();
                        }
                    }
                case S: // shared string
                    return getSharedStringValue();
                case N:
                case B:
                case E:
                default:
                    String v = cell.getV();
                    if (v != null) {
                        return v;
                    }
            }
            return "";
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to get cell value", e);
        }
    }

    private String getSharedStringValue() throws Docx4JException {
        List<CTRst> stringItems = sharedStrings.getContents().getSi();

        int index;
        try {
            index = Integer.parseInt(cell.getV());
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(cell.getV() + " can't be converted to an index into the shared strings table");
        }

        CTRst rst = stringItems.get(index);
        if (rst.getR().size() > 0
                && rst.getT() != null) {
            throw new IllegalStateException("Unable to get shared string cell value, contains 2 types of data");
        }

        if (rst.getT() != null) {
            return rst.getT().getValue();
        }

        // build value
        StringBuilder sb = new StringBuilder();
        for (CTRElt rElt : rst.getR()) {

            sb.append(rElt.getT().getValue()); // TODO worry about whitespace
        }

        return sb.toString();
    }

    public void setCellValue(String newValue) {
        try {
            STCellType type = cell.getT();
            switch (type) {
                case STR: // formula
                    cell.setF(null);
                    cell.setV(null);
                    setInlineString(newValue);
                    break;
                case INLINE_STR: // inline (rich) string
                    CTRst is = cell.getIs();
                    if (is != null) {
                        CTXstringWhitespace t = is.getT();
                        if (t != null) {
                            t.setValue(newValue);
                        }
                    }
                    break;
                case S: // shared string
                    setSharedStringValue(newValue);
                    break;
                case N:
                case B:
                case E:
                default:
                    setInlineString(newValue);
            }
        } catch (Docx4JException e) {
            throw new IllegalStateException("Unable to get cell value", e);
        }
    }

    private void setInlineString(String newValue) {
        CTXstringWhitespace newT = new CTXstringWhitespace();
        newT.setValue(newValue);
        CTRst newIs = new CTRst();
        newIs.setT(newT);
        cell.setT(STCellType.INLINE_STR);
        cell.setIs(newIs);
    }

    private void setSharedStringValue(String newValue) throws Docx4JException {
        List<CTRst> stringItems = sharedStrings.getContents().getSi();

        int idx = stringItems.indexOf(newValue);
        if (idx < 0) {
            CTRst rst = new CTRst();
            CTXstringWhitespace newT = new CTXstringWhitespace();
            newT.setValue(newValue);
            rst.setT(newT);
            stringItems.add(rst);
            idx = stringItems.size() - 1;
        }

        cell.setV(String.valueOf(idx));
    }

    /*
     * takes in a column reference portion  converts it from ALPHA-26 number format to 0-based base 10.
     * 'A' -> 0 'Z' -> 25 'AA' -> 26 'IV' -> 255
     * Returns: zero based column index
     */
    public int getColumnIndex() {
        if (columnIndex == null) {
            parseAddress();
        }
        return columnIndex;
    }

    private void parseAddress() {
        String address = getAddress();
        Matcher cellRefPatternMatcher = CELL_REF_PATTERN.matcher(address);
        if (!cellRefPatternMatcher.matches()) {
            throw new IllegalStateException("Unable to determine cell address");
        }
        String lettersGroup = cellRefPatternMatcher.group(1);
        columnString = lettersGroup;
        parseColumnString(lettersGroup);
        String digitsGroup = cellRefPatternMatcher.group(2);
        rowIndex = Integer.parseInt(digitsGroup) - 1;
    }

    private void parseColumnString(String ref) {
        int pos = 0;
        int retval=0;
        for (int k = ref.length()-1; k >= 0; k--) {
            char thechar = ref.charAt(k);
            if (thechar == ABSOLUTE_REFERENCE_MARKER) {
                if (k != 0) {
                    throw new IllegalArgumentException("Bad col ref format '" + ref + "'");
                }
                break;
            }
            // Character.getNumericValue() returns the values
            //  10-35 for the letter A-Z
            int shift = (int)Math.pow(26, pos);
            retval += (Character.getNumericValue(thechar)-9) * shift;
            pos++;
        }
        columnIndex = retval-1;
    }

    public String getColumnString() {
        if (columnString == null) {
            parseAddress();
        }
        return columnString;
    }

    public int getRowIndex() {
        if (rowIndex == null) {
            parseAddress();
        }
        return rowIndex;
    }

    public String getAddress() {
        return cell.getR();
    }
}
