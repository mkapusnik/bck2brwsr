/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package sun.tools.javap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/** An abstract parser for annotation definitions. Analyses the bytes and
 * performs some callbacks to the overriden parser methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AnnotationParser {
    protected AnnotationParser() {
    }

    protected void visitAttr(String type, String attr, String value) {
    }
    
    /** Initialize the parsing with constant pool from <code>cd</code>.
     * 
     * @param attr the attribute defining annotations
     * @param cd constant pool
     * @throws IOException in case I/O fails
     */
    public final void parse(byte[] attr, ClassData cd) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(attr);
        DataInputStream dis = new DataInputStream(is);
        try {
            read(dis, cd);
        } finally {
            is.close();
        }
    }
    
    private void read(DataInputStream dis, ClassData cd) throws IOException {
    	int cnt = dis.readUnsignedShort();
        for (int i = 0; i < cnt; i++) {
            readAnno(dis, cd);
        }
    }

    private void readAnno(DataInputStream dis, ClassData cd) throws IOException {
        int type = dis.readUnsignedShort();
        String typeName = cd.StringValue(type);
    	int cnt = dis.readUnsignedShort();
    	for (int i = 0; i < cnt; i++) {
            readCmp(dis, cd, typeName);
        }
    }

    private void readCmp(DataInputStream dis, ClassData cd, String typeName) 
    throws IOException {
        String name = cd.StringValue(dis.readUnsignedShort());
        char type = (char)dis.readByte();
        if (type == '@') {
            readAnno(dis, cd);
        } else if ("CFJZsSIDB".indexOf(type) >= 0) { // NOI18N
            int primitive = dis.readUnsignedShort();
            visitAttr(typeName, name, cd.StringValue(primitive));
        } else if (type == 'c') {
            int cls = dis.readUnsignedShort();
        } else if (type == '[') {
            int cnt = dis.readUnsignedShort();
            for (int i = 0; i < cnt; i++) {
                readCmp(dis, cd, typeName);
            }
        } else if (type == 'e') {
            int enumT = dis.readUnsignedShort();
            int enumN = dis.readUnsignedShort();
        } else {
            throw new IOException("Unknown type " + type);
        }
    }
}
