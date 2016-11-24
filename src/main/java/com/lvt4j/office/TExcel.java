/**
 * 封装的poi中excel操作，需要poi.jar支持
 * 作者：李晨曦
 */
package com.lvt4j.office;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class TExcel implements Closeable {
    
    private static final int CREATE_FILE = -1;
    private static final int CREATE_SHEET = 0;
    private static final int CREATE_ROW = 1;
    private static final int CREATE_COL = 2;
    
    private static final Map<Class<?>, Integer> valClsCellType = new HashMap<Class<?>, Integer>();
    static{
        valClsCellType.put(int.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(Integer.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(long.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(Long.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(double.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(Double.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(float.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(Float.class, HSSFCell.CELL_TYPE_NUMERIC);
        valClsCellType.put(String.class, HSSFCell.CELL_TYPE_STRING);
    }
    
    /** 写文件的输出流 */
    private FileOutputStream xlsFos;    
    /** poi 中webbook */
    private HSSFWorkbook wb;
    /** 记录上一次创建操作 */
    private int previousCreate;
    /** 当前操作sheet */
    private HSSFSheet sheet;
    /** 当前操作行 */
    private HSSFRow row;
    /** 当前单元格在当前操作行中序号 */
    private int cellRowIdx;
    /** 当前操作列对应行 */
    private HSSFRow rowS[];
    /** 当前操作列序号 */
    private short colIdx;
    /** 当前单元格在当前操作列中序号 */
    private short cellColIdx;
    /** 当前操作sheet中行总数 */
    private int rowNum;
    
    public HSSFCellStyle centerCellStyle;
    
    public void open(String filePath){
        wb = new HSSFWorkbook();
        centerCellStyle = wb.createCellStyle();
        centerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        try {
            xlsFos = new FileOutputStream(filePath);
            previousCreate = CREATE_FILE;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
            wb.write(xlsFos);
            xlsFos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void createSheet(String sheetName) {
        sheet = wb.createSheet(sheetName);
        rowNum = 0;
        cellRowIdx = 0;
        cellColIdx = 0;
        previousCreate = CREATE_SHEET;
    }
    
    public void createRow(){
        if (previousCreate == CREATE_FILE) {
            System.err.println("请先创建sheet！");
            return ;
        }
        row = sheet.createRow(rowNum++);
        cellRowIdx = 0;
        cellColIdx = 0;
        previousCreate = CREATE_ROW;
    }
    
    public void createCol(short cellNum) {
        switch (previousCreate) {
        case CREATE_COL:
            if(rowS.length!=cellNum){
                System.err.println("多次创建列，列单元格数不能不同！");
            }
            colIdx += 1;
            cellColIdx = 0;
            break;
        case CREATE_ROW:
            rowS = new HSSFRow[cellNum];
            for (int i = 0; i < cellNum; i++) {
                rowS[i] = sheet.createRow(this.rowNum++);
            }
            colIdx = 0;
            cellColIdx = 0;
            break;
        default:
            System.err.println("没有sheet！");
            return;
        }
        previousCreate = CREATE_COL;
    }
    
    @SuppressWarnings("deprecation")
    public void addCell(Object cellValue) {
        HSSFCell cell;
        switch (previousCreate) {
        case CREATE_ROW:
            cell = row.createCell(cellRowIdx++);
            setCellVal(cell, cellValue);
            cell.setCellStyle(centerCellStyle);
            break;
        case CREATE_COL:
            if (cellColIdx>rowS.length) {
                System.err.println("超出当前列容纳最大单元格数！\n"
                        + "当前列最大单元格数："+rowS.length+"；预写入单元格位置:"+cellColIdx);
                return ;
            }
            cell = rowS[cellColIdx++].createCell(colIdx);
            setCellVal(cell, cellValue);
            cell.setCellStyle(centerCellStyle);
            break;
        default:
            System.err.println("请先创建行或列！");
            break;
        }
    }
    

    private static void setCellVal(HSSFCell cell, Object val) {
        if(val==null) val="";
        Integer cellType = valClsCellType.get(val.getClass());
        if(cellType==null) cellType=HSSFCell.CELL_TYPE_STRING;
        switch (cellType) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                double numVal = Double.valueOf(String.valueOf(val));
                cell.setCellValue(numVal);
                break;
            default:
                String strVal = String.valueOf(val);
                cell.setCellValue(strVal);
                break;
        }
    }
    
    public static void main(String[] args) {
        TExcel excel = new TExcel();
        excel.open("d:\\1.xls");
        excel.createSheet("1");
        excel.createRow();
        excel.addCell("2");
        excel.addCell("3");
        excel.close();
    }
}
