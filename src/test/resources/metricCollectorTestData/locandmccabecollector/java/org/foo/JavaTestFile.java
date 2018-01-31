package org.foo;

import java.util.HashMap;

public class JavaTestFile {
    JavaTestFile() {
        System.out.println();
    }

    public void myMethod() {};

    public void ifMethod() {
        // compl: 3
        if(true) {

        } else {

            if (false) {
                System.out.println();
            } else {
                System.out.println();
            }
        }

    }

    public void whileTest() {
        // compl: 4
        while(true) {};
        while(!done) {
            while(true) {
                ;
            }
        }
    }

    public void forTest() {
        // compl: 3
        for(int i=0; i<99; i++) {
            for(String str: allStrings) {
                System.out.println(str);
            }
        }
    }

    public void switchTest() {
        // compl: 8
        switch(x) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            default:
                break;
        }
    }

    public void foreachCatchTest() {
        // compl: 4
        try {

        } catch (Exception e) {}
        catch (Throwable t) {
            t.printStackTrace();
            try {} catch(Throwable ignore) {System.out.println();}
        } finally {}
    }

    public void expressionTest() {
        // compl: 3
        //asd
        /*
         * asd
         * asd
         * asda
         * asd
         */
        boolean done = x && y;
        boolean ready = x < 10;
        boolean error = y <= 0 || z == 1;
    }

    public void ternaryOperatorTest() {
        /*
        compl= 2
        comment none
         */
        int value = ready ? 0 : 1;
    }

    public void complete1()  {
        // compl: 11
        if (a == b)  {
            if (a1 == b1) {
                fiddle();
            } else if (a2 == b2) {
                fiddle();
            }  else {
                fiddle();
            }
        } else if (c == d) {
            while (c == d) {
                fiddle();
            }
        } else if (e == f) {
            for (int n = 0; n < h; n++) {
                fiddle();
            }
        } else{
            switch (z) {
                case 1:
                    fiddle();
                    break;
                case 2:
                    fiddle();
                    break;
                case 3:
                    fiddle();
                    break;
                default:
                    fiddle();
                    break;
            }
        }
    }

    public void complete2() {
        // compl: 12
        // asd

        try {
            if(true) {}
            for(String x : xes) {
                switch(x) {
                    case 0:
                        break;
                    case 1:
                        break;
                    default:
                        boolean done = x && y;
                        boolean open = y == 0 || z;
                        String name = x ? "X" : "None";
                        String value = value ? "X" : "bad";
                }
            }
        }
        catch (Exception e) {
            while(!done) {}
        }
        catch (Throwable t) {
            t.printStackTrace();

        }
        finally {}
    }
}