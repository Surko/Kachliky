/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kachliky;

import cz.matfyz.sykora.nngui.data.DataProvider;
import cz.matfyz.sykora.nngui.exception.DataException;

/**
 *
 * @author kirrie
 */
public class MyProvider implements DataProvider{

    private double[][] rows;
    private double[] actualRow;
    private int actualIndex; 
    
    public MyProvider(double[][] data) {
        rows = data;
        actualIndex = 0;
    }
    
    @Override
    public void close() {
        
    }

    @Override
    public boolean fetchRow() throws DataException {
        if (actualIndex >= rows.length) {
            return false;
        }           
        actualRow = rows[actualIndex];
        return true;
    }

    @Override
    public int getRowSize() {
        if (actualRow == null) {
            if (rows.length > 0) {
                return rows[0].length;
            }
        }
        return actualRow.length;        
    }

    @Override
    public double getValue(int _index) throws DataException {
        if (_index >= actualRow.length) {
            throw new DataException("Index je vacsi ako dlzka riadku");
        }
        return actualRow[_index];
    }

    public void setIndex(int index) {
        actualIndex = index;
    }
    
    public void incIndex() {
        actualIndex++;
    }
    
    public void decIndex() {
        actualIndex--;
    }
    
    @Override
    public boolean rewind() {
        actualIndex = 0;
        return true;
    }
    
    public int getActualIndex() {
        return actualIndex;
    }
    
}
