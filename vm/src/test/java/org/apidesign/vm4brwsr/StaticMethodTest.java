/*
Java 4 Browser Bytecode Translator
Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. Look for COPYING file in the top folder.
If not, see http://opensource.org/licenses/GPL-2.0.
*/
package org.apidesign.vm4brwsr;

import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Checks the basic behavior of the translator.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StaticMethodTest {
    @Test public void threePlusFour() throws Exception {
        assertExec(
            "Should be seven", 
            "org_apidesign_vm4brwsr_StaticMethod_sumIII", 
            Double.valueOf(7), 
            3, 4
        );
    }

    @Test public void powerOfThree() throws Exception {
        assertExec(
            "Should be nine", 
            "org_apidesign_vm4brwsr_StaticMethod_powerFF", 
            Double.valueOf(9),
            3.0f
        );
    }

    @Test public void minusOne() throws Exception {
        assertExec(
            "Should be minus one", 
            "org_apidesign_vm4brwsr_StaticMethod_minusOneI", 
            Double.valueOf(-1)
        );
    }

    @Test public void doubleWithoutLong() throws Exception {
        assertExec(
            "Should be two",
            "org_apidesign_vm4brwsr_StaticMethod_minusDDJ", 
            Double.valueOf(2),
            3.0d, 1l
        );
    }

    @Test public void divAndRound() throws Exception {
        assertExec(
            "Should be rounded to one",
            "org_apidesign_vm4brwsr_StaticMethod_divIBD", 
            Double.valueOf(1),
            3, 3.75
        );
    }
    @Test public void mixedMethodFourParams() throws Exception {
        assertExec(
            "Should be two",
            "org_apidesign_vm4brwsr_StaticMethod_mixIIJBD", 
            Double.valueOf(20),
            2, 10l, 5, 2.0
        );
    }
    @Test public void factRec() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            "org_apidesign_vm4brwsr_StaticMethod_factRecJI", 
            Double.valueOf(120),
            5
        );
    }
    @Test public void factIter() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            "org_apidesign_vm4brwsr_StaticMethod_factIterJI", 
            Double.valueOf(120),
            5
        );
    }
    
    @Test public void xor() throws Exception {
        assertExec(
            "Xor is 4",
            "org_apidesign_vm4brwsr_StaticMethod_xorJIJ",
            Double.valueOf(4),
            7,
            3
        );
    }
    
    @Test public void or() throws Exception {
        assertExec(
            "Or will be 7",
            "org_apidesign_vm4brwsr_StaticMethod_orOrAndJZII",
            Double.valueOf(7),
            true,
            4,
            3
        );
    }
    @Test public void nullCheck() throws Exception {
        assertExec(
            "Returns nothing",
            "org_apidesign_vm4brwsr_StaticMethod_noneLjava_lang_ObjectII",
            null, 1, 3
        );
    }
    @Test public void and() throws Exception {
        assertExec(
            "And will be 3",
            "org_apidesign_vm4brwsr_StaticMethod_orOrAndJZII",
            Double.valueOf(3),
            false,
            7,
            3
        );
    }
    @Test public void inc4() throws Exception {
        assertExec(
            "It will be 4",
            "org_apidesign_vm4brwsr_StaticMethod_inc4I",
            Double.valueOf(4)
        );
    }
    
    @Test public void shiftLeftInJava() throws Exception {
        int res = StaticMethod.shiftLeft(1, 8);
        assertEquals(res, 256);
    }

    @Test public void shiftLeftInJS() throws Exception {
        assertExec(
            "Setting 9th bit",
            "org_apidesign_vm4brwsr_StaticMethod_shiftLeftIII",
            Double.valueOf(256),
            1, 8
        );
    }

    @Test public void shiftRightInJava() throws Exception {
        int res = StaticMethod.shiftArithmRight(-8, 3, true);
        assertEquals(res, -1);
    }

    @Test public void shiftRightInJS() throws Exception {
        assertExec(
            "Get -1",
            "org_apidesign_vm4brwsr_StaticMethod_shiftArithmRightIIIZ",
            Double.valueOf(-1),
            -8, 3, true
        );
    }
    @Test public void unsignedShiftRightInJava() throws Exception {
        int res = StaticMethod.shiftArithmRight(8, 3, false);
        assertEquals(res, 1);
    }

    @Test public void unsignedShiftRightInJS() throws Exception {
        assertExec(
            "Get -1",
            "org_apidesign_vm4brwsr_StaticMethod_shiftArithmRightIIIZ",
            Double.valueOf(1),
            8, 3, false
        );
    }
    
    @Test public void javaScriptBody() throws Exception {
        assertExec(
            "JavaScript string",
            "org_apidesign_vm4brwsr_StaticMethod_i2sLjava_lang_StringII",
            "333",
            330, 3
        );
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        Invocable i = compileClass(sb, "org/apidesign/vm4brwsr/StaticMethod");
        
        Object ret = null;
        try {
            ret = i.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes != null && expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + sb);
        
    }

    static Invocable compileClass(StringBuilder sb, String... names) throws ScriptException, IOException {
        if (sb == null) {
            sb = new StringBuilder();
        }
        GenJS.compile(sb, names);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByExtension("js");
        try {
            Object res = js.eval(sb.toString());
            assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            return (Invocable)js;
        } catch (ScriptException ex) {
            fail("Could not compile:\n" + sb, ex);
            return null;
        }
    }
}