package com.metacodestudio.hotsuploader.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Hero {

    @JsonProperty("PrimaryName")
    private String primaryName;
    @JsonProperty("ImageURL")
    private String imageURL;

    public Hero() {
    }

    public Hero(final String primaryName, final String imageURL) {
        this.primaryName = primaryName;
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return primaryName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Hero hero = (Hero) o;

        return !(primaryName != null ? !primaryName.equals(hero.primaryName) : hero.primaryName != null)
                && !(imageURL != null ? !imageURL.equals(hero.imageURL) : hero.imageURL != null);

    }

    @Override
    public int hashCode() {
        int result = primaryName != null ? primaryName.hashCode() : 0;
        result = 31 * result + (imageURL != null ? imageURL.hashCode() : 0);
        return result;
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public void setPrimaryName(final String primaryName) {
        this.primaryName = primaryName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(final String imageURL) {
        this.imageURL = imageURL;
    }

}
