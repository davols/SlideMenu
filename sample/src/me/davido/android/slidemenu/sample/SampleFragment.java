package me.davido.android.slidemenu.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Sample fragment for textviews layout at this point. 
 */
public class SampleFragment extends Fragment{
	private int mType;

	public SampleFragment(){
		
	}
	/**
	 * OnCreate, fetch type to show. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getArguments() != null ? getArguments().getInt("type") : 0;
	}
	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{  
		if(mType==0){
			View view = inflater.inflate(R.layout.layout_textview, container, false);  
			return view;
		}
		else if(mType==1) {
			View view = inflater.inflate(R.layout.two_fragments, container, false);  
			return view;
		}
		else {
			return null;
		}

	}
}
