package com.robosoft.archana.youtubeapplication.Util;

import com.robosoft.archana.youtubeapplication.Constants;

/**
 * Created by archana on 2/4/16.
 */
public class Upload {
    public static String generateKeywordFromPlaylistId(String playlistId) {
        if (playlistId == null) playlistId = "";
        if (playlistId.indexOf("PL") == 0) {
            playlistId = playlistId.substring(2);
        }
        playlistId = playlistId.replaceAll("\\W", "");
        String keyword = Constants.DEFAULT_KEYWORD.concat(playlistId);
        if (keyword.length() > Constants.MAX_KEYWORD_LENGTH) {
            keyword = keyword.substring(0, Constants.MAX_KEYWORD_LENGTH);
        }
        return keyword;
    }

}
