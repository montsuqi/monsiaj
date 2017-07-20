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
package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.InterfaceBuildingException;
import org.montsuqi.monsiaj.monsia.SignalData;
import org.montsuqi.monsiaj.monsia.SignalInfo;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.widgets.Button;
import org.montsuqi.monsiaj.widgets.Calendar;
import org.montsuqi.monsiaj.widgets.CheckBox;
import org.montsuqi.monsiaj.widgets.ColorButton;
import org.montsuqi.monsiaj.widgets.Entry;
import org.montsuqi.monsiaj.widgets.FileChooserButton;
import org.montsuqi.monsiaj.widgets.Fixed;
import org.montsuqi.monsiaj.widgets.Frame;
import org.montsuqi.monsiaj.widgets.HBox;
import org.montsuqi.monsiaj.widgets.HPaned;
import org.montsuqi.monsiaj.widgets.HSeparator;
import org.montsuqi.monsiaj.widgets.Notebook;
import org.montsuqi.monsiaj.widgets.NumberEntry;
import org.montsuqi.monsiaj.widgets.PandaCList;
import org.montsuqi.monsiaj.widgets.PandaCombo;
import org.montsuqi.monsiaj.widgets.PandaDownload;
import org.montsuqi.monsiaj.widgets.PandaEntry;
import org.montsuqi.monsiaj.widgets.PandaHTML;
import org.montsuqi.monsiaj.widgets.PandaPreview;
import org.montsuqi.monsiaj.widgets.PandaTable;
import org.montsuqi.monsiaj.widgets.PandaText;
import org.montsuqi.monsiaj.widgets.PandaTimer;
import org.montsuqi.monsiaj.widgets.Pixmap;
import org.montsuqi.monsiaj.widgets.RadioButton;
import org.montsuqi.monsiaj.widgets.Table;
import org.montsuqi.monsiaj.widgets.ToggleButton;
import org.montsuqi.monsiaj.widgets.VBox;
import org.montsuqi.monsiaj.widgets.VPaned;
import org.montsuqi.monsiaj.widgets.VSeparator;
import org.montsuqi.monsiaj.widgets.Window;

/**
 * <p>
 * Super class for all widget builders.</p>
 *
 * <p>
 * A widget builder is a class which provides methods to create instances of a
 * specific class.</p>
 */
public class WidgetBuilder {

    protected static final Logger logger = LogManager.getLogger(WidgetBuilder.class);
    private static Map<String, Class> classMap;
    private static Map<String, WidgetBuilder> builderMap;

    /**
     * <p>
     * Maps a generic(toolkit independent) widget class name, to actual Java
     * class and widget builder.</p>
     *
     * @param genericClassName a toolkit independent widget type like 'Button'
     * or 'List'.
     * @param clazz a class adopted to perform as the given generic class in
     * Java.
     * @param builder a widget builder which will build the given type of
     * widgets.
     */
    private static void registerWidgetClass(String genericClassName, Class clazz, WidgetBuilder builder) {
        classMap.put(genericClassName, clazz);
        builderMap.put(genericClassName, builder);
    }

    // set up the widget builder map
    static {
        builderMap = new HashMap<>();
        classMap = new HashMap<>();

        WidgetBuilder defaultWidgetBuilder = new WidgetBuilder();
        WidgetBuilder defaultContainerBuilder = new ContainerBuilder();
        WidgetBuilder entryBuilder = new EntryBuilder();

        registerWidgetClass("Button", Button.class, defaultWidgetBuilder);
        registerWidgetClass("Calendar", Calendar.class, defaultWidgetBuilder);
        registerWidgetClass("CheckButton", CheckBox.class, new CheckButtonBuilder());
        registerWidgetClass("Dialog", Window.class, new WindowBuilder());
        registerWidgetClass("Entry", Entry.class, entryBuilder);
        registerWidgetClass("FileChooserButton", FileChooserButton.class, defaultWidgetBuilder);
        registerWidgetClass("ColorButton", ColorButton.class, defaultWidgetBuilder);
        registerWidgetClass("Fixed", Fixed.class, new FixedBuilder());
        registerWidgetClass("Frame", Frame.class, new FrameBuilder());
        registerWidgetClass("HBox", HBox.class, defaultContainerBuilder);
        registerWidgetClass("VPaned", HPaned.class, defaultContainerBuilder);
        registerWidgetClass("HSeparator", HSeparator.class, defaultWidgetBuilder);
        registerWidgetClass("Label", JLabel.class, new LabelBuilder());
        registerWidgetClass("Notebook", Notebook.class, new NotebookBuilder());
        registerWidgetClass("NumberEntry", NumberEntry.class, entryBuilder);
        registerWidgetClass("PandaCombo", PandaCombo.class, new PandaComboBuilder());
        registerWidgetClass("PandaCList", PandaCList.class, new CListBuilder());
        registerWidgetClass("PandaEntry", PandaEntry.class, entryBuilder);
        registerWidgetClass("PandaHTML", PandaHTML.class, defaultWidgetBuilder);
        registerWidgetClass("PandaPS", PandaPreview.class, defaultWidgetBuilder);
        registerWidgetClass("PandaText", PandaText.class, new PandaTextBuilder());
        registerWidgetClass("PandaTimer", PandaTimer.class, defaultWidgetBuilder);
        registerWidgetClass("PandaDownload", PandaDownload.class, defaultWidgetBuilder);
        registerWidgetClass("PandaTable", PandaTable.class, defaultWidgetBuilder);
        registerWidgetClass("Pixmap", Pixmap.class, defaultWidgetBuilder);
        registerWidgetClass("Placeholder", JPanel.class, defaultWidgetBuilder);
        registerWidgetClass("ProgressBar", JProgressBar.class, defaultWidgetBuilder);
        registerWidgetClass("RadioButton", RadioButton.class, new RadioButtonBuilder());
        registerWidgetClass("ScrolledWindow", JScrollPane.class, new ScrolledWindowBuilder());
        registerWidgetClass("Table", Table.class, new TableBuilder());
        registerWidgetClass("Text", PandaText.class, new PandaTextBuilder());
        registerWidgetClass("ToggleButton", ToggleButton.class, defaultContainerBuilder);
        registerWidgetClass("VBox", VBox.class, defaultContainerBuilder);
        registerWidgetClass("VPaned", VPaned.class, defaultContainerBuilder);
        registerWidgetClass("VSeparator", VSeparator.class, defaultWidgetBuilder);
        registerWidgetClass("Viewport", JViewport.class, new ViewportBuilder());
        registerWidgetClass("Window", Window.class, new WindowBuilder());
    }

    // an interface to make a modified font.
    private interface FontModifier {

        Font modifyFont(Font font);
    }

    // set up UI resources
    static {
        String[] fontlist = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] classes = {
            "Button",
            "ToggleButton",
            "RadioButton",
            "ComboBox",
            "CheckBox",
            "RadioButton",
            "TabbedPane",
            "Label",
            "TextField",
            "FormattedTextField",
            "TextArea",
            "Table",
            "ToolBar",
            "TitledBorder",
            "ToolTip",
            "ProgressBar",
            "List",};
        for (String classe : classes) {
            String userFontSpec = System.getProperty("monsia.user.font." + classe.toLowerCase(Locale.ENGLISH));
            if (userFontSpec == null) {
                userFontSpec = System.getProperty("monsia.user.font"); //$NON-NLS-1
            }
            if (userFontSpec == null) {
                Arrays.sort(fontlist);
                if (Arrays.binarySearch(fontlist, "メイリオ") >= 0) {
                    userFontSpec = "メイリオ-PLAIN-12";
                } else if (Arrays.binarySearch(fontlist, "ＭＳ ゴシック") >= 0) {
                    userFontSpec = "ＭＳ ゴシック-PLAIN-12";
                } else {
                    userFontSpec = "Monospaced-PLAIN-12";
                }
            }
            final Font userFont = Font.decode(userFontSpec);
            modifyFont(classe, new FontModifier() {

                @Override
                public Font modifyFont(Font font) {
                    return userFont;
                }
            });
        }
    }

    private static void modifyFont(String className, FontModifier creator) {
        String key = className + ".font";
        Font font = (Font) UIManager.get(key);
        if (font != null) {
            font = creator.modifyFont(font);
            UIManager.put(key, font);
        }
    }

    /**
     * <p>
     * Builds a widget.</p>
     *
     *
     * <p>
     * A widget is build in following steps:</p> <ol> <li>Build Self. Build the
     * widget itself using the widget info and other stuff. This is performed by
     * buildSelf method. Properties in widget info are set. Accelerators are set
     * too.</li> <li>Build Children. Build children widgets in it. This is
     * performed by buildChildren method. Basic procedure of building children
     * is defined in ContainerBuilder widget builder.</li>
     * <li>Names are assigned.</li> <li>Signals are set.</li> </ol>
     *
     * @param xml glade interface definition.
     * @param info widget info for the target widget.
     * @param parent parent component of the target widget.
     * @return a widget.
     */
    public static Component buildWidget(Interface xml, WidgetInfo info, Container parent) {
        String genericClassName = info.getClassName();
        WidgetBuilder builder = (WidgetBuilder) builderMap.get(genericClassName);
        if (builder == null) {
            logger.warn("unknown widget class: {0}", genericClassName);
            Object[] args = {genericClassName};
            return new JLabel(MessageFormat.format("[a {0}]", args));
        }
        try {
            Component widget = builder.buildSelf(xml, parent, info);
            if (widget instanceof Window) {
                xml.setTopLevel(widget);
            }
            builder.setSignals(xml, widget, info);
            if (widget instanceof Container) {
                builder.buildChildren(xml, (Container) widget, info);
            }
            if (System.getProperty("monsia.debug.widget.border") != null) {
                if (widget instanceof JComponent) {
                    if (!(widget instanceof JViewport)) {
                        JComponent jcomponent = (JComponent) widget;
                        if (jcomponent.getBorder() == null) {
                            jcomponent.setBorder(BorderFactory.createEtchedBorder());
                        }
                    }
                }
            }
            return widget;
        } catch (Exception e) {
            logger.warn(e);
            return new JLabel('[' + e.toString() + ']');
        }
    }

    /**
     * <p>
     * Instantiate a specific component.</p>
     * <p>
     * When one is created successfuly, properties are set and accelerators are
     * assigned.</p>
     *
     * @param xml glade screen definition.
     * @param parent parent widget.
     * @param info widget info.
     * @return constructed widget.
     */
    Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
        String genericClassName = info.getClassName();
        Class clazz = (Class) classMap.get(genericClassName);
        if (clazz == null) {
            throw new IllegalArgumentException(genericClassName);
        }
        try {
            Component widget = (Component) clazz.newInstance();
            setCommonParameters(xml, widget, info);
            setProperties(xml, parent, widget, info.getProperties());
            xml.addAccels(widget, info);
            return widget;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.fatal(e);
            throw new InterfaceBuildingException(e);
        }
    }

    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        // do nothing
    }

    protected void setCommonParameters(Interface xml, Component widget, WidgetInfo info) {
        String name = info.getName();

        while (xml.containWidgetLongNameTable(info.getLongName())) {
            name += "_";
            info.setName(name);
            System.out.println("name overlap! rename to " + info.getLongName());
        }

        widget.setName(info.getLongName());
        xml.setWidgetNameTable(info.getName(), widget);
        xml.setWidgetLongNameTable(info.getLongName(), widget);
        xml.setProperties(info.getLongName(), info.getProperties());
    }

    protected void setSignals(Interface xml, Component widget, WidgetInfo info) {
        Iterator i = info.getSignals().iterator();
        while (i.hasNext()) {
            xml.addSignal(new SignalData(widget, ((SignalInfo) i.next())));
        }
    }

    void setProperties(Interface xml, Container parent, Component widget, Map properties) {
        Class clazz = widget.getClass();
        Iterator i = properties.entrySet().iterator();
        String positionValue = null;
        while (i.hasNext()) {
            Map.Entry ent = (Map.Entry) i.next();
            String name = (String) ent.getKey();
            String value = (String) ent.getValue();
            if ("position".equals(name)) {
                positionValue = value;
                continue; // set position after size of this window is determined.
            }
            WidgetPropertySetter setter = WidgetPropertySetter.getSetter(clazz, name);
            setter.set(xml, parent, widget, value);
        }
        if (positionValue != null) {
            WidgetPropertySetter setter = WidgetPropertySetter.getSetter(clazz, "position");
            setter.set(xml, parent, widget, positionValue);
        }
    }
}
