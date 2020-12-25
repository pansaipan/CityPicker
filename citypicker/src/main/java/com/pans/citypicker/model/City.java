package com.pans.citypicker.model;

import java.util.ArrayList;

public class City {
	private String name;
	private ArrayList<County> counties = new ArrayList<>();


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<County> getCounties() {
		return counties;
	}

	public void setCounties(ArrayList<County> counties) {
		this.counties = counties;
	}
}
