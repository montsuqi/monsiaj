/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

 This module is part of PANDA.

 PANDA is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
 to anyone for the consequences of using it or for whether it serves
 any particular purpose or works at all, unless he says so in writing.
 Refer to the GNU General Public License for full details.

 Everyone is granted permission to copy, modify and redistribute
 PANDA, but only under the conditions described in the GNU General
 Public License.  A copy of this license is supposed to have been given
 to you along with PANDA so you can know your rights and
 responsibilities.  It should be in a file named COPYING.  Among other
 things, the copyright notice and this notice must be preserved on all
 copies.
 */
package org.montsuqi.monsiaj.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * <
 * p>
 * A FileFilter that filters by file name extension.</p>
 *
 * <p>
 * This filter accepts files whose names end with the specified extension. All
 * directories (with any name) are also accepted.</p>
 */
public class ExtensionFileFilter extends FileFilter {

    private final String extension;
    private final String description;

    /**
     * <
     * p>
     * Constructs a ExtensionFileFilter that filters by given extension.</p>
     *
     * @param extension extension which is accepted.
     * @param description descriptive text that explains this filter.
     */
    public ExtensionFileFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    /**
     * <
     * p>
     * Returns true when the given file is a directory or its name ends with the
     * extension. False otherwise.</p>
     *
     * @param f a File to test.
     * @return true when the file is acceptable by this filter, false otherwise.
     */
    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getPath().endsWith(extension);
    }

    /**
     * <
     * p>
     * Returns textual description of this filter.</p>
     *
     * @return a descriptive text.
     */
    @Override
    public String getDescription() {
        return description;
    }
}
