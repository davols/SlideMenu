package me.davido.android.slidemenu.sample;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MyListFragment extends ListFragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, Shakespeare.TITLES));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		((MainActivity) getActivity()).onClicked(position);

	}

}

