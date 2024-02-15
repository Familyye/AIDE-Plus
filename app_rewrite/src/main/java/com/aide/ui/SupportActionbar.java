package com.aide.ui;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.SpinnerAdapter;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentTransaction;
import io.github.zeroaicy.util.reflect.ReflectPie;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

public class SupportActionbar extends android.app.ActionBar{

	androidx.appcompat.app.ActionBar supportActionBar;
	ReflectPie actionBarReflect;

	public SupportActionbar(androidx.appcompat.app.ActionBar supportActionBar){
		this.supportActionBar = supportActionBar; 
		this.actionBarReflect = ReflectPie.on(supportActionBar);

	}


	public static SupportActionbar getSupportActionbar(ActionBar supportActionBar){
		if ( supportActionBar == null ){
			return null;
		}
		return new SupportActionbar(supportActionBar);
	}
	/*
	 * 装箱Tab
	 */
	Map<ActionBar.Tab, Tab> packTabMap = new HashMap<>();
	public Tab pack(ActionBar.Tab tabImpl){

		SupportActionbar.Tab packTab = packTabMap.get(tabImpl);
		if ( packTab == null ){
			packTab = new Tab(tabImpl);
			packTabMap.put(tabImpl, packTab);
		}
		return packTab;
	}
	/*
	 * 拆箱Tab
	 */
	public static ActionBar.Tab unPack(android.app.ActionBar.Tab tab){
		return ((SupportActionbar.Tab)tab).tabImpl;
	}

	final Map<WeakReference<android.app.ActionBar.TabListener>, WeakReference<TabListener>> packTabListenerMap = new HashMap<>();
	/*
	 * 装箱 TabListener
	 */
	public TabListener pack(android.app.ActionBar.TabListener tabListener){
		WeakReference<android.app.ActionBar.TabListener> 
			key = new WeakReference<android.app.ActionBar.TabListener>(tabListener);
		WeakReference<TabListener> value = packTabListenerMap.get(key);

		SupportActionbar.TabListener packTab = null;
		if ( value != null ){
			packTab = value.get();
		}
		if ( packTab == null ){
			packTab = new TabListener(tabListener);
			packTabListenerMap.put(key, new WeakReference<TabListener>(packTab));
		}
		return packTab;
	}

	public void setHasEmbeddedTabs(boolean hasEmbeddedTabs){
		actionBarReflect.call("setHasEmbeddedTabs", hasEmbeddedTabs);
	}


	@Override
	public void addOnMenuVisibilityListener(final android.app.ActionBar.OnMenuVisibilityListener listener){
		this.supportActionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener(){
				@Override
				public void onMenuVisibilityChanged(boolean isVisible){
					listener.onMenuVisibilityChanged(isVisible);
				}
			});
	}
	
	/**
	 * 非抽象方法区
	 */
	@Override
	public Context getThemedContext() {
		return this.supportActionBar.getThemedContext();
	}

	@Override
	public float getElevation() {
		return this.supportActionBar.getElevation();
	}

	@Override
	public int getHideOffset() {
		return this.supportActionBar.getHideOffset();
	}

	@Override
	public boolean isHideOnContentScrollEnabled() {
		return this.supportActionBar.isHideOnContentScrollEnabled();
	}

	@Override
	public void setElevation(float elevation) {
		
		this.supportActionBar.setElevation(elevation);
	}

	@Override
	public void setHideOffset(int offset) {
		
		this.supportActionBar.setHideOffset(offset);
	}

	@Override
	public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
		
		this.supportActionBar.setHideOnContentScrollEnabled(hideOnContentScroll);
	}

	@Override
	public void setHomeActionContentDescription(int resId) {
		
		this.supportActionBar.setHomeActionContentDescription(resId);
	}

	@Override
	public void setHomeActionContentDescription(CharSequence description) {
		
		this.supportActionBar.setHomeActionContentDescription(description);
	}

	@Override
	public void setHomeAsUpIndicator(int resId) {
		
		this.supportActionBar.setHomeAsUpIndicator(resId);
	}

	@Override
	public void setHomeAsUpIndicator(Drawable indicator) {
		
		this.supportActionBar.setHomeAsUpIndicator(indicator);
	}

	@Override
	public void setHomeButtonEnabled(boolean enabled) {
		
		this.supportActionBar.setHomeButtonEnabled(enabled);
	}

	@Override
	public void setSplitBackgroundDrawable(Drawable d) {
		
		this.supportActionBar.setSplitBackgroundDrawable(d);
	}

	@Override
	public void setStackedBackgroundDrawable(Drawable d) {
		this.supportActionBar.setStackedBackgroundDrawable(d);
	}
	
	
	
	/**
	 * 抽象方法区
	 */
	private ArrayList<android.app.ActionBar.Tab> mTabs = new ArrayList<>();

	@Override
	public void addTab(android.app.ActionBar.Tab tab){
		this.supportActionBar.addTab(unPack(tab));
		this.mTabs.add(tab);
	}

	@Override
	public void addTab(android.app.ActionBar.Tab tab, int position){
		this.supportActionBar.addTab(unPack(tab), position);
		this.mTabs.add(position, tab);
	}

	@Override
	public void addTab(android.app.ActionBar.Tab tab, int position, boolean setSelected){
		this.supportActionBar.addTab(unPack(tab), position, setSelected);
		this.mTabs.add(position, tab);
	}

	@Override
	public void addTab(android.app.ActionBar.Tab tab, boolean setSelected){
		this.supportActionBar.addTab(unPack(tab), setSelected);
		this.mTabs.add(tab);
	}

	@Override
	public View getCustomView(){
		return this.supportActionBar.getCustomView();
	}

	@Override
	public int getDisplayOptions(){
		return this.supportActionBar.getDisplayOptions();
	}

	@Override
	public int getHeight(){
		return this.supportActionBar.getHeight();
	}

	@Override
	public int getNavigationItemCount(){
		return this.supportActionBar.getNavigationItemCount();
	}

	@Override
	public int getNavigationMode(){
		return this.supportActionBar.getNavigationMode();
	}

	@Override
	public int getSelectedNavigationIndex(){
		return this.supportActionBar.getSelectedNavigationIndex();
	}

	@Override
	public android.app.ActionBar.Tab getSelectedTab(){
		return pack(this.supportActionBar.getSelectedTab());
	}

	@Override
	public CharSequence getSubtitle(){
		return this.supportActionBar.getSubtitle();
	}

	@Override
	public android.app.ActionBar.Tab getTabAt(int index){
		this.supportActionBar.getTabAt(index);
		return mTabs.get(index);
	}

	@Override
	public int getTabCount(){
		return this.supportActionBar.getTabCount();
	}

	@Override
	public CharSequence getTitle(){
		return this.supportActionBar.getTitle();
	}

	@Override
	public void hide(){
		this.supportActionBar.hide();
	}

	@Override
	public boolean isShowing(){
		return this.supportActionBar.isShowing();
	}

	@Override
	public android.app.ActionBar.Tab newTab(){
		return pack(this.supportActionBar.newTab());
	}

	@Override
	public void removeAllTabs(){
		this.mTabs.clear();
		this.packTabMap.clear();
		this.supportActionBar.removeAllTabs();
	}

	@Override
	public void removeOnMenuVisibilityListener(final android.app.ActionBar.OnMenuVisibilityListener listener){
		this.supportActionBar.removeOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener(){
				@Override
				public void onMenuVisibilityChanged(boolean isVisible){
					listener.onMenuVisibilityChanged(isVisible);
				}
			});
	}

	@Override
	public void removeTab(android.app.ActionBar.Tab tab){
		this.mTabs.remove(tab);
		this.supportActionBar.removeTab(SupportActionbar.unPack(tab));
	}

	@Override
	public void removeTabAt(int position){
		this.mTabs.remove(position);
		this.supportActionBar.removeTabAt(position);
	}

	@Override
	public void selectTab(android.app.ActionBar.Tab tab){
		this.supportActionBar.selectTab(SupportActionbar.unPack(tab));
	}

	@Override
	public void setBackgroundDrawable(Drawable d){
		this.supportActionBar.setBackgroundDrawable(d);
	}

	@Override
	public void setCustomView(int resId){
		this.supportActionBar.setCustomView(resId);
	}

	@Override
	public void setCustomView(View view){
		this.supportActionBar.setCustomView(view);
	}

	@Override
	public void setCustomView(View view, android.app.ActionBar.LayoutParams layoutParams){
		this.supportActionBar.setCustomView(view, new ActionBar.LayoutParams(layoutParams.width, layoutParams.height, layoutParams.gravity));
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp){
		this.supportActionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
	}

	@Override
	public void setDisplayOptions(int options){
		this.supportActionBar.setDisplayOptions(options);
	}

	@Override
	public void setDisplayOptions(int options, int mask){
		this.supportActionBar.setDisplayOptions(options, mask);
	}

	@Override
	public void setDisplayShowCustomEnabled(boolean showCustom){
		this.supportActionBar.setDisplayShowCustomEnabled(showCustom);
	}

	@Override
	public void setDisplayShowHomeEnabled(boolean showHome){
		this.supportActionBar.setDisplayShowHomeEnabled(showHome);
	}

	@Override
	public void setDisplayShowTitleEnabled(boolean showTitle){
		this.supportActionBar.setDisplayShowTitleEnabled(showTitle);
	}

	@Override
	public void setDisplayUseLogoEnabled(boolean useLogo){
		this.supportActionBar.setDisplayUseLogoEnabled(useLogo);
	}

	@Override
	public void setIcon(int resId){
		this.supportActionBar.setIcon(resId);
	}

	@Override
	public void setIcon(Drawable icon){
		this.supportActionBar.setIcon(icon);
	}

	@Override
	public void setListNavigationCallbacks(SpinnerAdapter adapter, final android.app.ActionBar.OnNavigationListener callback){
		this.supportActionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener(){
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId){
					return callback.onNavigationItemSelected(itemPosition, itemId);
				}
			});
	}

	@Override
	public void setLogo(int resId){
		this.supportActionBar.setLogo(resId);
	}

	@Override
	public void setLogo(Drawable logo){
		this.supportActionBar.setLogo(logo);
	}

	@Override
	public void setNavigationMode(int mode){
		this.supportActionBar.setNavigationMode(mode);
	}

	@Override
	public void setSelectedNavigationItem(int position){
		this.supportActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public void setSubtitle(int resId){
		this.supportActionBar.setSubtitle(resId);
	}

	@Override
	public void setSubtitle(CharSequence subtitle){
		this.supportActionBar.setSubtitle(subtitle);
	}

	@Override
	public void setTitle(int resId){
		this.supportActionBar.setTitle(resId);
	}

	@Override
	public void setTitle(CharSequence title){
		this.supportActionBar.setTitle(title);
	}

	@Override
	public void show(){
		this.supportActionBar.show();
	}

	public class Tab extends android.app.ActionBar.Tab{

		public final ActionBar.Tab tabImpl;

		public Tab(ActionBar.Tab tabImpl){
			this.tabImpl = tabImpl;
		}

		@Override
		public CharSequence getContentDescription(){
			return this.tabImpl.getContentDescription();
		}

		@Override
		public View getCustomView(){
			return this.tabImpl.getCustomView();
		}

		@Override
		public Drawable getIcon(){
			return this.tabImpl.getIcon();
		}

		@Override
		public int getPosition(){
			return tabImpl.getPosition();
		}

		@Override
		public Object getTag(){
			return this.tabImpl.getTag();
		}

		@Override
		public CharSequence getText(){
			return this.tabImpl.getText();
		}

		@Override
		public void select(){
			this.tabImpl.select();
		}

		@Override
		public android.app.ActionBar.Tab setContentDescription(int resId){
			this.tabImpl.setContentDescription(resId);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setContentDescription(CharSequence contentDesc){
			this.tabImpl.setContentDescription(contentDesc);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setCustomView(int layoutResId){
			this.tabImpl.setCustomView(layoutResId);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setCustomView(View view){
			this.tabImpl.setCustomView(view);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setIcon(int resId){
			this.tabImpl.setIcon(resId);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setIcon(Drawable icon){
			this.tabImpl.setIcon(icon);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setTabListener(android.app.ActionBar.TabListener listener){
			this.tabImpl.setTabListener(pack(listener));
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setTag(Object obj){
			this.tabImpl.setTag(obj);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setText(int resId){
			this.tabImpl.setText(resId);
			return this;
		}

		@Override
		public android.app.ActionBar.Tab setText(CharSequence text){
			this.tabImpl.setText(text);
			return this;
		}
	}

	public class TabListener implements ActionBar.TabListener{
		/*
		 * 好在AIDE没有使用监听器的FragmentTransaction参数
		 * 因此传入null, 否则需要重写的东西太多了
		 * 就有Fragment，没必要
		 */
		final android.app.ActionBar.TabListener tabListener;
		public TabListener(android.app.ActionBar.TabListener tabListener){
			this.tabListener = tabListener;
		}
		@Override
		public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft){
			this.tabListener.onTabReselected(pack(tab), null);
		}

		@Override
		public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft){
			this.tabListener.onTabSelected(pack(tab), null);
		}

		@Override
		public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft){
			this.tabListener.onTabUnselected(pack(tab), null);
		}
	}
}
