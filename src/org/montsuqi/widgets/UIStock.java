/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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

package org.montsuqi.widgets;

import java.net.URL;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class UIStock {

	private static Map stocks;
	private String text;
	private String tooltip;
	private Icon icon;

	private UIStock(String text, String tooltip) {
		this(text, tooltip, null);
	}

	private UIStock(String text, String tooltip, String iconFile) {
		if (text == null) {
			throw new NullPointerException("null text"); //$NON-NLS-1$
		}
		if (tooltip == null) {
			throw new NullPointerException("null tooltip"); //$NON-NLS-1$
		}
		this.text = text;
		this.tooltip = tooltip;

		if (iconFile != null) {
			String iconPath = "/org/montsuqi/widgets/images/" + iconFile + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
			URL url = getClass().getResource(iconPath);
			icon = new ImageIcon(url);
		}
	}

	public String getText() {
		return text;
	}

	public String getToolTip() {
		return tooltip;
	}

	public Icon getIcon() {
		return icon;
	}

	public static UIStock get(String key) {
		if (stocks.containsKey(key)) {
			return (UIStock)stocks.get(key);
		}
		throw new IllegalArgumentException("stock not found"); //$NON-NLS-1$
	}

	static {
		stocks.put("FILE_TREE",         new UIStock(Messages.getString("UIStock.File"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("NEW_ITEM",          new UIStock(Messages.getString("UIStock.New"), "", "new")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("NEW_TREE",          new UIStock(Messages.getString("UIStock.New..."), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("OPEN_ITEM",         new UIStock(Messages.getString("UIStock.Open..."), Messages.getString("UIStock.OpenTip"), "open")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("SAVE_ITEM",         new UIStock(Messages.getString("UIStock.Save"), Messages.getString("UIStock.SaveTip"), "save")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("SAVE_AS_ITEM",      new UIStock(Messages.getString("UIStock.SaveAs..."), Messages.getString("UIStock.SaveAsTip"), "save_as")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("REVERT_ITEM",       new UIStock(Messages.getString("UIStock.Revert"), Messages.getString("UIStock.RevertTip"), "revert")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("PRINT_ITEM",        new UIStock(Messages.getString("UIStock.Print"), Messages.getString("UIStock.PrintTip"), "print")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("PRINT_SETUP_ITEM",  new UIStock(Messages.getString("UIStock.PrintSetup..."), Messages.getString("UIStock.PrintSetupTip"), "print")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("CLOSE_ITEM",        new UIStock(Messages.getString("UIStock.Close"), Messages.getString("UIStock.CloseTip"), "close")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("EXIT_ITEM",         new UIStock(Messages.getString("UIStock.Exit"), Messages.getString("UIStock.ExitTip"), "exit")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		stocks.put("EDIT_TREE",         new UIStock(Messages.getString("UIStock.Edit"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("CUT_ITEM",          new UIStock(Messages.getString("UIStock.Cut"), Messages.getString("UIStock.CutTip"), "cut")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("COPY_ITEM",         new UIStock(Messages.getString("UIStock.Copy"), Messages.getString("UIStock.CopyTip"), "copy")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("PASTE_ITEM",        new UIStock(Messages.getString("UIStock.Paste"), Messages.getString("UIStock.PasteTip"), "paste")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("SELECT_ALL_ITEM",   new UIStock(Messages.getString("UIStock.SelectAll"), Messages.getString("UIStock.SelectAllTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("CLEAR_ITEM",        new UIStock(Messages.getString("UIStock.Clear"), Messages.getString("UIStock.ClearTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("UNDO_ITEM",         new UIStock(Messages.getString("UIStock.Undo"), Messages.getString("UIStock.UndoTip"), "undo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("REDO_ITEM",         new UIStock(Messages.getString("UIStock.Redo"), Messages.getString("UIStock.RedoTip"), "redo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("FIND_ITEM",         new UIStock(Messages.getString("UIStock.Find..."), Messages.getString("UIStock.FindTip"), "search")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("FIND_AGAIN_ITEM",   new UIStock(Messages.getString("UIStock.FindAgain"), Messages.getString("UIStock.FindAgainTip"), "search")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("REPLACE_ITEM",      new UIStock(Messages.getString("UIStock.Replace..."), Messages.getString("UIStock.ReplaceTip"), "search_replace")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("PROPERTIES_ITEM",   new UIStock(Messages.getString("UIStock.Properties..."), Messages.getString("UIStock.PropertiesTip"), "properties")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		stocks.put("VIEW_TREE",         new UIStock(Messages.getString("UIStock.View"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		stocks.put("SETTINGS_TREE",     new UIStock(Messages.getString("UIStock.Settings"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("PREFERENCES_ITEM",  new UIStock(Messages.getString("UIStock.Preerences..."), Messages.getString("UIStock.PreferencesTip"), "preferences")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		stocks.put("FILES_TREE",        new UIStock(Messages.getString("UIStock.Files"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		stocks.put("WINDOWS_TREE",      new UIStock(Messages.getString("UIStock.Windows"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("NEW_WINDOW_ITEM",   new UIStock(Messages.getString("UIStock.CreateNewWindow"), Messages.getString("UIStock.CreateNewWindwowTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("CLOSE_WINDOW_ITEM", new UIStock(Messages.getString("UIStock.CloseThisWindow"), Messages.getString("UIStock.CloseThisWindowTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		stocks.put("HELP_TREE",         new UIStock(Messages.getString("UIStock.Help"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("ABOUT_ITEM",        new UIStock(Messages.getString("UIStock.About..."), Messages.getString("UIStock.AboutTip"), "about")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		stocks.put("GAME_TREE",         new UIStock(Messages.getString("UIStock.Game"), "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("NEW_GAME_ITEM",     new UIStock(Messages.getString("UIStock.NewGame"), Messages.getString("UIStock.NewGameTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("PAUSE_GAME_ITEM",   new UIStock(Messages.getString("UIStock.PauseGame"), Messages.getString("UIStock.PauseGameTip"), "pause")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("RESTART_GAME_ITEM", new UIStock(Messages.getString("UIStock.RestartGame"), Messages.getString("UIStock.RestartGameTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("UNDO_MOVE_ITEM",    new UIStock(Messages.getString("UIStock.UndoMove"), Messages.getString("UIStock.UndoMoveTip"), "undo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("REDO_MOVE_ITEM",    new UIStock(Messages.getString("UIStock.RedoMove"), Messages.getString("UIStock.RedoMoveTip"), "redo")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("HINT_ITEM",         new UIStock(Messages.getString("UIStock.Hint"), Messages.getString("UIStock.HintTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		stocks.put("SCORE_ITEM",        new UIStock(Messages.getString("UIStock.Score..."), Messages.getString("UIStock.ScoreTip"), "scores")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		stocks.put("END_GAME_ITEM",     new UIStock(Messages.getString("UIStock.EndGame"), Messages.getString("UIStock.EndGameTip"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
