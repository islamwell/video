package com.nurulquran.audio.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryMusic implements Serializable{
	private int id;
	private String title;
	private String image;
	private List<Song> listSongs;
	private String idParent;
	private String countSub;
	private String level;

	public String getIdParent() {
		return idParent;
	}

	public void setIdParent(String idParent) {
		this.idParent = idParent;
	}

	public String getCountSub() {
		return countSub;
	}

	public void setCountSub(String countSub) {
		this.countSub = countSub;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<Song> getListSongs() {
		if (listSongs == null) {
			return new ArrayList<Song>();
		}
		return listSongs;
	}

	public void setListSongs(List<Song> listSongs) {
		this.listSongs = listSongs;
	}

	public void addListSongs(List<Song> listSongs) {
		this.listSongs.addAll(listSongs);
	}

	public void addSong(Song song) {
		if (listSongs == null) {
			listSongs = new ArrayList<Song>();
		}
		listSongs.add(song);
	}

	public void clearSong() {
		if (listSongs == null) {
			listSongs = new ArrayList<Song>();
		} else {
			listSongs.clear();
		}
	}

	
}
