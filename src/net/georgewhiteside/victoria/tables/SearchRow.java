package net.georgewhiteside.victoria.tables;

import net.georgewhiteside.victoria.VideoGame;

public class SearchRow implements Comparable<SearchRow> {
	private float score;
	private final VideoGame videoGame;
	
	public SearchRow(VideoGame videoGame, float score) {
		this.videoGame = videoGame;
		this.score = score;
	}
	
	public float getScore() { return score; }
	
	public VideoGame getVideoGame() { return videoGame; }
	
	public void setScore(float value) { score = value; }

	@Override
	public int compareTo(SearchRow o) {
		SearchRow thisGame = this;
		SearchRow thatGame = o;
		
		// two VideoGames could have the same match score but still be distinct set items;
		// so if the score is equal, fall back to the guaranteed-unique db primary key

		if(thisGame.equals(thatGame)) {
			return 0;
		}
		
		if(thisGame.getScore() == thatGame.getScore()) {
			return thisGame.getVideoGame().getId() < thatGame.getVideoGame().getId() ? -1 : 1;
		}
		
		return thisGame.getScore() < thatGame.getScore() ? -1 : 1;
	}
	
	@Override
	public String toString() {
		return Float.toString(score) + " " + videoGame.getTitle();
	}
}