package com.example.geocaching;

/*
 * This is the expandable list adapter that will take the 
 * list of caches as an input and create an ExpandableListView for 
 * easy reading for the user
 */
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Activity _context;
	private List<String> _listDataHeader; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<String>> _listDataChild;
	private ExpandableListView _listView;
	private int lastExpandedGroupPosition;

	public ExpandableListAdapter(Activity context, List<String> listDataHeader,
			HashMap<String, List<String>> listChildData,
			ExpandableListView listView) {
		this._context = context;
		this._listDataHeader = listDataHeader;
		this._listDataChild = listChildData;
		this._listView = listView;

	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.BaseExpandableListAdapter#onGroupExpanded(int)
	 * 
	 * Only one group can be expanded at once. Thus, there can only be one selected
	 * cache which will be the one chosen when the user starts a search
	 */
	@Override
	public void onGroupExpanded(int groupPosition) {
		// collapse the old expanded group, if not the same
		// as new group to expand
		if (groupPosition != lastExpandedGroupPosition) {
			this._listView.collapseGroup(lastExpandedGroupPosition);
		}

		super.onGroupExpanded(groupPosition);
		lastExpandedGroupPosition = groupPosition;
	}
	
	/*
	 * Returns the Expandable List View
	 */

	public ExpandableListView getListView() {
		return _listView;

	}

	/*
	 * Returns last expanded cache
	 */
	public int getLastExpanded() {
		return lastExpandedGroupPosition;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChild(int, int)
	 * 
	 * Returns the cache detail given the group and child positions
	 */
	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this._listDataChild.get(
				this._listDataHeader.get(groupPosition).trim()).get(
				childPosititon);
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
	 * 
	 * Returns the cache detail ID given the group and child positions
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 * 
	 * Returns the cache detail view given the group and child positions as well
	 * as if it is the last child and other variables. Inflates items in the layout
	 * and sets the text of the textview
	 */

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final String childText = (String) getChild(groupPosition, childPosition);
		convertView = null;

		if (childText.equals("buttons")) {
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.last_list_item,
						null);
			}
		}

		else {
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.list_item, null);
			}

			TextView txtListChild = (TextView) convertView
					.findViewById(R.id.lblListItem);

			txtListChild.setText(childText);

		}
		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
	 * 
	 * Returns children count
	 */
	@Override
	public int getChildrenCount(int groupPosition) {
		return this._listDataChild.get(
				this._listDataHeader.get(groupPosition).trim()).size();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroup(int)
	 * 
	 * Group is returned given the position
	 */
	@Override
	public Object getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupCount()
	 * 
	 * Returns the group count
	 */
	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupId(int)
	 * 
	 * Returns the group ID given the position
	 */
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
	 * 
	 * Converts a view into a list_group layout. The textview is then modified.
	 */

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_group, null);
		}

		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.lblListHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}