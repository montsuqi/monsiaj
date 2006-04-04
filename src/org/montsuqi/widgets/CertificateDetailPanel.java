package org.montsuqi.widgets;

import java.awt.BorderLayout;
import java.awt.Font;
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

public class CertificateDetailPanel extends JPanel {

	JTable table;
	JTextArea text;
	JList list;
	X509Certificate[] chain;

	public CertificateDetailPanel() {
		initComponents();
	}

	public void setCertificateChain(final X509Certificate[] chain) {
		this.chain = chain;
		ListModel listModel = new CertificateListModel(chain);
		list.setModel(listModel);
		ListSelectionModel selection = list.getSelectionModel();
		selection.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int selected = list.getSelectedIndex();
				setFocusedCertificate(CertificateDetailPanel.this.chain[selected]);
			}
		});
		list.setSelectedIndex(0);
	}

	public void setFocusedCertificate(final X509Certificate certificate) {
		TableModel model = new CertificateTableModel(certificate);
		ListSelectionModel oldSelection = table.getSelectionModel();
		int oldSelectionIndex = oldSelection.getLeadSelectionIndex();
		table.setModel(model);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		ListSelectionModel selection = table.getSelectionModel();
		selection.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if ( ! e.getValueIsAdjusting()) {
					final int selectedRow = table.getSelectedRow();
					if (0 <= selectedRow && selectedRow < table.getRowCount()) {
						Object value = table.getValueAt(selectedRow, CertificateTableModel.VALUE_COLUMN);
						text.setText(value.toString());
					}
				}
			}
		});
		selection.setSelectionInterval(oldSelectionIndex, oldSelectionIndex);
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		text = new JTextArea(7, 4 + 1 + 16 * 3);
		Font font = text.getFont();
		int style = font.getSize();
		int size = font.getSize();
		font = new Font("Monospaced", style, size);
		text.setFont(font);

		final JScrollPane listScroll = new JScrollPane(list);
		add(listScroll, BorderLayout.WEST);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		final JScrollPane tableScroll = new JScrollPane(table);
		panel.add(tableScroll, BorderLayout.NORTH);

		final JScrollPane textScroll = new JScrollPane(text);
		panel.add(textScroll, BorderLayout.SOUTH);
		add(panel, BorderLayout.EAST);
	}
} 

class CertificateListModel extends AbstractListModel {

	private X509Certificate[] chain;

	CertificateListModel(X509Certificate[] chain) {
		this.chain = chain;
	}

	public int getSize() {
		return chain.length;
	}

	public Object getElementAt(int index) {
		final X509Certificate certificate = chain[index];
		final X500Principal issuerPrincipal = certificate.getIssuerX500Principal();
		final String issuerName = getCommonName(issuerPrincipal);
		final X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		final String subjectName = getCommonName(subjectPrincipal);
		return subjectName + " (" + issuerName + ")";
	}

	private String getCommonName(X500Principal principal) {
		final String distingishName = principal.getName();
		final Pattern pattern = Pattern.compile("CN\\s*=\\s*([^;,\\s]+)", Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(distingishName);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "NOT KNOWN"; //$NON-NLS-1$
		}
	}
}

class CertificateTableModel extends AbstractTableModel {

	String[] fieldNames = {
		Messages.getString("CertificateDetailPanel.Version"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Serial"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Algorithm"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Issuer"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Validity"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Subject"), //$NON-NLS-1$
		Messages.getString("CertificateDetailPanel.Signature") //$NON-NLS-1$
	};

	private static final int VERSION_FIELD = 0;
	private static final int SERIAL_FIELD = 1;
	private static final int ALGORITHM_FIELD = 2;
	private static final int ISSUER_FIELD = 3;
	private static final int VALIDITY_FIELD = 4;
	private static final int SUBJECT_FIELD = 5;
	private static final int SIGNATURE_FIELD = 6;

	private String[] columnNames = {
			Messages.getString("CertificateDetailPanel.Field"), //$NON-NLS-1$
			Messages.getString("CertificateDetailPanel.Value") //$NON-NLS-1$
	};

	private static final int FIELD_COLUMN = 0;
	static final int VALUE_COLUMN = 1;

	private X509Certificate certificate;

	public CertificateTableModel(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public int getRowCount() {
		return fieldNames.length;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public Class getColumnClass(int columnIndex) {
		return String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case FIELD_COLUMN:
			return fieldNames[rowIndex];
		case VALUE_COLUMN:
			return getFieldValueFor(rowIndex);
		}
		throw new IllegalArgumentException("column is out of range."); //$NON-NLS-1$
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
			final Format format = new MessageFormat("[{0}, {1}]"); //$NON-NLS-1$
			final Object[] args = new Object[] {notBefore, notAfter };
			return format.format(args);
		case SUBJECT_FIELD:
			return certificate.getSubjectDN();
		case SIGNATURE_FIELD:
			return formatSignature(certificate.getSignature());
		default:
			throw new IllegalArgumentException("row out of range."); //$NON-NLS-1$
		}
	}

	private String formatSignature(byte[] signature) {
		StringBuffer buf = new StringBuffer();
		int rows = signature.length / 16;
		for (int row = 0; row < rows; row++) {
			String offset = ("0000" + Integer.toHexString(row * 16)); //$NON-NLS-1$
			offset = offset.substring(offset.length() - "0000".length()); //$NON-NLS-1$
			byte[] rowBytes = new byte[16];
			System.arraycopy(signature, row * 16, rowBytes, 0, 16);
			buf.append(offset);
			buf.append(':');
			appendBytes(buf, rowBytes);
			buf.append('\n');
		}
		return buf.toString();
	}

	private void appendBytes(StringBuffer buf, byte[] rowBytes) {
		for (int i = 0; i < rowBytes.length; i++) {
			String hex = ("00" + Integer.toHexString(rowBytes[i])); //$NON-NLS-1$
			hex = hex.substring(hex.length() - "00".length()); //$NON-NLS-1$
			buf.append(" "); //$NON-NLS-1$
			buf.append(hex);
		}
	}
}
