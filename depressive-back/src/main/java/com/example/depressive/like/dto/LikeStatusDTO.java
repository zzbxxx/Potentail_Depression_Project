package com.example.depressive.like.dto;

public class LikeStatusDTO {
    private boolean isLiked;
    private long likeCount;

    public LikeStatusDTO(boolean isLiked, long likeCount) {
        this.isLiked = isLiked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        this.isLiked = liked;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
