// Copyright 2015-2016 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.utils;

import ninja.eivind.stormparser.models.Replay;
import ninja.eivind.stormparser.models.Player;
import ninja.eivind.stormparser.models.PlayerType;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayUtils {
    private static UUID getUUIDForString(String concatenatedString) throws NoSuchAlgorithmException {
        final byte[] hashed = MessageDigest.getInstance("MD5").digest(concatenatedString.getBytes());
        final byte[] reArranged = reArrangeForUUID(hashed);
        return getUUID(reArranged);
    }

    private static byte[] reArrangeForUUID(byte[] hashed) {
        return new byte[]{
                hashed[3],
                hashed[2],
                hashed[1],
                hashed[0],

                hashed[5],
                hashed[4],
                hashed[7],
                hashed[6],
                hashed[8],
                hashed[9],
                hashed[10],
                hashed[11],
                hashed[12],
                hashed[13],
                hashed[14],
                hashed[15],
        };
    }

    private static UUID getUUID(byte[] bytes) {
        long msb = 0;
        long lsb = 0;
        assert bytes.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }

    private static String getConcatenatedString(Replay replay) {
        final String randomValue = String.valueOf(replay.getInitData().getRandomValue());
        final List<String> battleNetIdsSorted = replay.getReplayDetails()
                .getPlayers()
                .stream()
                .map(Player::getBNetId)
                .map(Long::parseLong)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.toList());
        final StringBuilder builder = new StringBuilder();
        battleNetIdsSorted.forEach(builder::append);
        builder.append(randomValue);
        return builder.toString();
    }

    /**
     * Compute the replay ID, in the format shared by hotslogs and hotsapi
     */
    public static String getMatchId(Replay replay) throws NoSuchAlgorithmException {
        return getUUIDForString(getConcatenatedString(replay)).toString();
    }

    public static boolean replayHasComputerPlayers(Replay replay) {
        return replay.getReplayDetails()
                .getPlayers()
                .stream()
                .map(Player::getPlayerType)
                .anyMatch(playerType -> playerType == PlayerType.COMPUTER);
    }
}
