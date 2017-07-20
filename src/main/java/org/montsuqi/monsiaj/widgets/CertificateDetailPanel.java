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
package org.montsuqi.monsiaj.widgets;
import org.montsuqi.monsiaj.util.Messages;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * <
 * p>
 * A component that shows details of a certificate.</p>
 *
 * <p>
 * On the left it holds the certificate chain list. On top right the contents
 * summary of the selected certificate and on the bottom right detail of the
 * selected element of the current certificate.</p>
 */
public class CertificateDetailPanel extends JPanel {

    static final int BYTES_IN_ROW = 16;
    JTable table;
    JTextArea text;
    JList list;
    X509Certificate[] chain;

    public CertificateDetailPanel() {
        initComponents();
    }

    public void setCertificateChain(final X509Certificate[] chain) {
        this.chain = (X509Certificate[]) chain.clone();
        final ListModel listModel = new CertificateListModel();
        list.setModel(listModel);
        final ListSelectionModel selection = list.getSelectionModel();
        selection.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final int selected = list.getSelectedIndex();
                if (0 <= selected && selected < CertificateDetailPanel.this.chain.length) {
                    final X509Certificate certificate = CertificateDetailPanel.this.chain[selected];
                    setFocusedCertificate(certificate);
                }
            }
        });
        list.setSelectedIndex(0);
    }

    public void setFocusedCertificate(final X509Certificate certificate) {
        final TableModel model = new CertificateTableModel(certificate);
        final ListSelectionModel oldSelection = table.getSelectionModel();
        final int oldSelectionIndex = oldSelection.getLeadSelectionIndex();
        table.setModel(model);
        final Dimension preferredSize = table.getPreferredSize();
        table.setPreferredScrollableViewportSize(preferredSize);
        final ListSelectionModel selection = table.getSelectionModel();
        selection.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    final int selectedRow = table.getSelectedRow();
                    if (0 <= selectedRow && selectedRow < table.getRowCount()) {
                        final Object value = table.getValueAt(selectedRow, CertificateTableModel.VALUE_COLUMN);
                        text.setText(value.toString());
                        text.setCaretPosition(0);
                    }
                }
            }
        });
        selection.setSelectionInterval(oldSelectionIndex, oldSelectionIndex);
    }

    static final int BYTE_WIDTH = "00".length();
    static final int HEADING_WIDTH = "0000".length();

    private void initComponents() {
        setLayout(new BorderLayout());

        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final int keyLength = 128;
        final int rows = keyLength / BYTES_IN_ROW;
        final int columns = HEADING_WIDTH + 1 + BYTES_IN_ROW * (1 + BYTE_WIDTH) + 1 /* center gap width */;
        text = new JTextArea(rows + 1, columns + 1); /* +1 to count in scrollbar width */

        final Font font = text.getFont();
        final int style = font.getSize();
        final int size = font.getSize();
        final Font monospaceFont = new Font("Monospaced", style, size);
        text.setFont(monospaceFont);
        text.setEditable(false);

        final JScrollPane listScroll = new JScrollPane(list);
        add(listScroll, BorderLayout.WEST);

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        final JScrollPane tableScroll = new JScrollPane(table);
        panel.add(tableScroll, BorderLayout.NORTH);

        final JScrollPane textScroll = new JScrollPane(text);
        panel.add(textScroll, BorderLayout.SOUTH);
        add(panel, BorderLayout.EAST);
    }

    class CertificateListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return chain.length;
        }

        @Override
        public Object getElementAt(int index) {
            final X509Certificate certificate = chain[index];
            final X500Principal issuerPrincipal = certificate.getIssuerX500Principal();
            final String issuerName = getCommonName(issuerPrincipal);
            final X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
            final String subjectName = getCommonName(subjectPrincipal);
            final Object[] args = {subjectName, issuerName};
            final MessageFormat format = new MessageFormat("{0} ({1})");
            final String value = format.format(args);
            return value;
        }

        private String getCommonName(X500Principal principal) {
            final String distingishName = principal.getName();
            final Pattern pattern = Pattern.compile("CN\\s*=\\s*([^;,\\s]+)", Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(distingishName);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return null;
            }
        }
    }

    class CertificateTableModel extends AbstractTableModel {

        String[] fieldNames = {
            Messages.getString("CertificateDetailPanel.Version"),
            Messages.getString("CertificateDetailPanel.Serial"),
            Messages.getString("CertificateDetailPanel.Algorithm"),
            Messages.getString("CertificateDetailPanel.Issuer"),
            Messages.getString("CertificateDetailPanel.Validity"),
            Messages.getString("CertificateDetailPanel.Subject"),
            Messages.getString("CertificateDetailPanel.Signature"),
            Messages.getString("CertificateDetailPanel.SHA1Fingerprint")
        };

        private static final int VERSION_FIELD = 0;
        private static final int SERIAL_FIELD = 1;
        private static final int ALGORITHM_FIELD = 2;
        private static final int ISSUER_FIELD = 3;
        private static final int VALIDITY_FIELD = 4;
        private static final int SUBJECT_FIELD = 5;
        private static final int SIGNATURE_FIELD = 6;
        private static final int FINGERPRINT_FIELD = 7;

        private final String[] columnNames = {
            Messages.getString("CertificateDetailPanel.Field"),
            Messages.getString("CertificateDetailPanel.Value")
        };

        private static final int FIELD_COLUMN = 0;
        static final int VALUE_COLUMN = 1;

        private final X509Certificate certificate;

        public CertificateTableModel(X509Certificate certificate) {
            this.certificate = certificate;
        }

        @Override
        public int getRowCount() {
            return fieldNames.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case FIELD_COLUMN:
                    return fieldNames[rowIndex];
                case VALUE_COLUMN:
                    return getFieldValueFor(rowIndex);
            }
            throw new IllegalArgumentException("column is out of range.");
        }

        private Object getFieldValueFor(int rowIndex) {
            switch (rowIndex) {
                case VERSION_FIELD:
                    return new Integer(certificate.getVersion());
                case SERIAL_FIELD:
                    return certificate.getSerialNumber();
                case ALGORITHM_FIELD:
                    return certificate.getSigAlgName();
                case ISSUER_FIELD:
                    return certificate.getIssuerDN();
                case VALIDITY_FIELD:
                    final Date notBefore = certificate.getNotBefore();
                    final Date notAfter = certificate.getNotAfter();
                    final Format format = new MessageFormat("[{0}, {1}]");
                    final Object[] args = {notBefore, notAfter};
                    return format.format(args);
                case SUBJECT_FIELD:
                    return certificate.getSubjectDN();
                case SIGNATURE_FIELD:
                    return formatSignature(certificate);
                case FINGERPRINT_FIELD:
                    return formatFingerprint(certificate);
                default:
                    throw new IllegalArgumentException("row out of range.");
            }
        }

        private String formatFingerprint(X509Certificate cert) {
            try {
                final MessageDigest digester = MessageDigest.getInstance("SHA");
                final byte[] digest = digester.digest(cert.getEncoded());
                final StringBuffer buf = new StringBuffer();
                for (int i = 0; i < digest.length; i++) {
                    if (i > 0) {
                        buf.append(':');
                    }
                    final int upper = (digest[i] >> 4) & 0xf;
                    final int lower = (digest[i]) & 0xf;
                    final String hex = Integer.toHexString(upper) + Integer.toHexString(lower);
                    buf.append(hex);
                }
                return buf.toString().toUpperCase();
            } catch (NoSuchAlgorithmException e) {
                return e.getMessage();
            } catch (CertificateEncodingException e) {
                return e.getMessage();
            }
        }

        private String formatSignature(X509Certificate cert) {
            byte[] signature = cert.getSignature();
            StringBuffer buf = new StringBuffer();
            int rows = signature.length / BYTES_IN_ROW;
            int rest = signature.length % BYTES_IN_ROW;
            for (int row = 0; row < rows; row++) {
                int offset = row * BYTES_IN_ROW;
                appendRow(buf, signature, offset, BYTES_IN_ROW);
            }
            if (rest > 0) {
                appendRow(buf, signature, signature.length - rest, rest);
            }
            return buf.toString();
        }

        private void appendRow(StringBuffer buf, byte[] bytes, int offset, int length) {
            final String heading = zeroPad(Integer.toHexString(offset), HEADING_WIDTH);
            buf.append(heading.toUpperCase());
            buf.append(':');
            for (int i = offset, half = offset + BYTES_IN_ROW / 2, end = offset + length; i < end; i++) {
                buf.append(' ');
                if (i == half) {
                    buf.append(' ');
                }
                final int upper = (bytes[i] >> 4) & 0xf;
                final int lower = (bytes[i]) & 0xf;
                final String hex = Integer.toHexString(upper) + Integer.toHexString(lower);
                buf.append(hex.toUpperCase());
            }
            buf.append('\n');
        }

        private String zeroPad(final String value, int width) {
            StringBuilder buf = new StringBuilder();
            for (int i = width - value.length(); i > 0; i--) {
                buf.append('0');
            }
            buf.append(value);
            return buf.toString();
        }
    }
}
