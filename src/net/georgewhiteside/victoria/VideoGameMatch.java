package net.georgewhiteside.victoria;

public class VideoGameMatch implements Comparable<VideoGameMatch> {
	private float score;
	private final VideoGame videoGame;
	
	public VideoGameMatch(VideoGame videoGame, float score) {
		this.videoGame = videoGame;
		this.score = score;
	}
	
	public double getScore() { return score; }
	
	public VideoGame getVideoGame() { return videoGame; }
	
	public void setScore(float value) { score = value; }

	@Override
	public int compareTo(VideoGameMatch o) {
		VideoGameMatch thisGame = this;
		VideoGameMatch thatGame = o;
		
		// two VideoGames could have the same match score but still be distinct set items;
		// so if the score is equal, fall back to the guaranteed-unique db primary key
		// comparator is reversed so the SortedSet iterator gives matches best-to-worst (or right-side correct for Levenshtein )

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