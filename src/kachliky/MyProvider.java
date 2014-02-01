/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kachliky;

import cz.matfyz.sykora.nngui.data.DataProvider;
import cz.matfyz.sykora.nngui.exception.DataException;
import java.util.Random;

/**
 *
 * @author kirrie
 */
public class MyProvider implements DataProvider{

    private double[][] rows;
    private double[] actualRow;
    
    private int[] permutation;
    private int permIndex;
    private int actualIndex; 
    
    public MyProvider(double[][] data) {
        rows = data;
        actualIndex = 0;
        permutation = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            permutation[i] = i;
        }
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
        permIndex++;
        if (permIndex >= permutation.length) {
            actualIndex = permutation.length + 1;
        } else {
            actualIndex = permutation[permIndex];
        }
    }
    
    public void decIndex() {
        permIndex--;
        if (permIndex >= permutation.length) {
            actualIndex = permutation.length + 1;
        } else {
            actualIndex = permutation[permIndex];
        }
    }        
    
    public void setPermutation(int[] permutation) {
        this.permutation = permutation;
    }
    
    public int[] getPermutation() {
        return permutation;
    }
    
    public void reroll() {
        Random random = new Random();        
        
        for (int i = permutation.length-1; i>0; i--) {
            int index = random.nextInt(i + 1);
            int a = permutation[index];
            permutation[index]=permutation[i];
            permutation[i]=a;
        }
        
    }
    
    @Override
    public boolean rewind() {        
        permIndex=0;
        actualIndex = permutation[permIndex];
        return true;
    }
    
    public int getActualIndex() {
        return actualIndex;
    }
    
}
