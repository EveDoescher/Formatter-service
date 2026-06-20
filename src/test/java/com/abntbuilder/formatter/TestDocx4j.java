package com.abntbuilder.formatter;

import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.Lvl;

public class TestDocx4j {
    public static void main(String[] args) {
        ObjectFactory f = new ObjectFactory();
        NumFmt fmt = f.createNumFmt();
        fmt.setVal(NumberFormat.DECIMAL);
        
        Lvl.LvlText text = f.createLvlLvlText();
        text.setVal("%1.");
    }
}
