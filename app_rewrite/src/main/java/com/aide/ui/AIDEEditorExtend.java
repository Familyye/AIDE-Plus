/**
 * @Author ZeroAicy
 * @AIDE AIDE+
*/
package com.aide.ui;
import com.aide.ui.views.CodeEditText;

public class AIDEEditorExtend{
	
	public static AIDEEditor getCurrentEditor(AIDEEditorPager aideEditorPager){
		return AIDEEditorPager.lp(aideEditorPager);
	}
	
	public static AIDEEditor.t getEditorModel(AIDEEditor aideEditor){
		return AIDEEditor.jJ(aideEditor);
	}
	
	public static CodeEditText.EditorView getEditorView(AIDEEditor aideEditor){
		return AIDEEditor.Ev(aideEditor);
	}
	
}
