package me.davido.android.slidemenu.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Simple main activity to be able to choose the different samples. 
 * 
 * @author david
 *
 */
public class MainActivity extends FragmentActivity{

	/**
	 * Available samples. 
	 */
	public static final String[] SAMPLES =
		{
		"TextViews",
		"ListFragments"

		};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_stack);

		if (savedInstanceState == null) {
			// Do first time initialization -- add initial fragment.

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.simple_fragment, new MyList()).commit();
		} 
	}

	public void onClicked(int position) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		SampleFragment newFragment = new SampleFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("type",position);
		newFragment.setArguments(bundle);
		ft.replace(R.id.simple_fragment, newFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack(null);
		ft.commit();
	}

	private class MyList extends ListFragment {
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setListAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, SAMPLES));
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			((MainActivity) getActivity()).onClicked(position);
		}
	}
}
