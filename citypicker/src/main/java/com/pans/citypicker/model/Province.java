package com.pans.citypicker.model;

import java.util.ArrayList;

public class Province {
	private String name;
	private ArrayList<City> cities = new ArrayList<>();


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<City> getCities() {
		return cities;
	}

	public void setCities(ArrayList<City> cities) {
		this.cities = cities;
	}
}
