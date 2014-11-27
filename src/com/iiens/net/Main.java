package com.iiens.net;

import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/** Main 
	Activit� pricipale 
	Permet de g�rer le menu lat�ral, certains param�tres g�n�raux ainsi que les transitions entre les diff�rentes fonctions de l'appli 
	Auteur : Srivatsan 'Loki' Magadevane, promo 2014
 **/

public class Main extends Activity {

	// Items shown on the menu, each corresponds to a Fragment
	private String[] menuItems = new String[]{
			"News",
			"Emploi du temps",
			"Anniversaires",
			"Twitter",
			"Trombinoscope"
	};
	// The Fragments corresponding to the items, based on the position in the list
	private Fragment[] menuFragments = new Fragment[]{
			new News(),
			new Edt(),
			new Anniv(),
			new TwitterNews(),
			new Trombi()
	};
	
	// The different ways of welcoming the user if connected, by replacing "..." by its name
	private String[] helloStrings = new String[]{
			"Bienvenue ... :)",
			"Coucou ... coucou",
			"Sup sup ... o/",
			"...",
			"Hello ... !",
			"..., SALOPE ! tu bois !"
	};
	
	private int defaultFragmentNumber = 0; // Number of the fragment to show first after the SplashScreen
	private String scriptURL = "*****"; // Address of the script making database queries

	private DrawerLayout drawerLayout;
	private ListView menu;
	private ActionBarDrawerToggle drawerToggle;

	private FragmentManager fragmentManager;
	private Fragment frag;
	private Bundle mainBundle;
	private boolean isConnected = false;
	private int currentFragment;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		mainBundle = new Bundle();
		mainBundle.putString("scriptURL", scriptURL);
		// If connected, get additional informations
		if (getIntent().getBooleanExtra("isConnected", false)) {
			isConnected = true;
			mainBundle.putString("login", getIntent().getStringExtra("login"));
			mainBundle.putString("pass", getIntent().getStringExtra("pass"));
			mainBundle.putString("nom", getIntent().getStringExtra("nom"));
		}
		currentFragment = defaultFragmentNumber;

		// Get back all info if the activity is recreated
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mainBundle.putAll(savedInstanceState);
		} 

		setContentView(R.layout.activity_main);

		// Creation of the side menu
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		menu = (ListView) findViewById(R.id.menu);
		createMenu();
	}

	@Override
	protected void onResume() {
		super.onResume();
		openFragment(currentFragment);
	}

	/* Action after (for ex) the screen orientation has been changed */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		currentFragment = savedInstanceState.getInt("currentFragment");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (drawerToggle.onOptionsItemSelected(item))
			return true;
		return super.onOptionsItemSelected(item);
	}

	/* Action when (for ex) the screen orientation changes */
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt("currentFragment", currentFragment);
		outState.putString("scriptURL", scriptURL);
		frag.onSaveInstanceState(outState);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		drawerToggle.onConfigurationChanged(newConfig);
	}

	/* Create the menu and add the items to it */
	private void createMenu() {
		// If connected, change initial parameters
		if (isConnected) setParametersConnected();

		ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuItems);
		menu.setAdapter(menuAdapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// Menu icon on the action bar
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 
				R.drawable.ic_menu, 
				R.string.open_menu, 
				R.string.close_menu);

		// Link the drawerToggle and the drawerLayout
		drawerLayout.setDrawerListener(drawerToggle);

		// Set the list's click listener
		menu.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override @SuppressWarnings("rawtypes")
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				if (currentFragment != position) {
					currentFragment = position;
					openFragment(position);
				}
				else drawerLayout.closeDrawer(menu);
			}
		});	
	}

	/* Specify the fragment to open based on the position of the menu item toggled */
	private void openFragment(int position) {
		fragmentManager = getFragmentManager();

		frag = menuFragments[position];
		if (frag != null) {
			mainBundle.putInt("currentFragment", currentFragment);
			if (!frag.isAdded()) frag.setArguments(mainBundle); // Can't setArguments if the fragment is active, isAdded verfies that
			fragmentManager.beginTransaction().replace(R.id.content, frag).commit();

			// Highlight the selected item, update the title, and close the drawer
			menu.setItemChecked(position, true);
			getActionBar().setTitle(menuItems[position]);
		}
		drawerLayout.closeDrawer(menu); 
	}
	
	/* Change the starting parameters to adapt to connected state */ 	
	private void setParametersConnected() {
		// Add random welcome message in first place
		String[] newMenuItems = new String[menuItems.length + 1];
		for (int i=0; i < menuItems.length; i++) {
			newMenuItems[i+1] = menuItems[i];
		}
		Random r = new Random();
		newMenuItems[0] = helloStrings[r.nextInt(helloStrings.length)].replace("...", mainBundle.getString("nom"));
		menuItems = newMenuItems;

		// Add a null fragment in first place to do nothing when toggled
		Fragment[] newMenuFragments = new Fragment[menuFragments.length + 1];
		for (int i=0; i < menuFragments.length; i++) {
			newMenuFragments[i+1] = menuFragments[i];
		}
		newMenuFragments[0] = null;
		menuFragments = newMenuFragments;

		currentFragment += 1;
	}
}
