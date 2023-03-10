package de.hdawg.rankinginfo.service.services;

import de.hdawg.rankinginfo.service.model.Ranking;
import de.hdawg.rankinginfo.service.model.listing.Listing;
import de.hdawg.rankinginfo.service.model.listing.ListingItem;
import de.hdawg.rankinginfo.service.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * service providing backend integration and mapping for ranking listing operations.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ListingService {

  private final RankingRepository rankingRepository;

  /**
   * retrieve the requested age groups. Filter results by given club String.
   *
   * @param rankingPeriod ranking period
   * @param ageGroup age group
   * @param gender gender
   * @param isYobRanking yob players only
   * @param overallRanking overall ranking including younger players
   * @param endOfYearRanking is end of year ranking
   * @param club club to filter for
   * @return listing container
   */
  public Listing getAgeGroupRankingsFilteredByClub(LocalDate rankingPeriod, String ageGroup, String gender, boolean isYobRanking, boolean overallRanking, boolean endOfYearRanking, String club) {
    Listing result = getAgeGroupRankings(rankingPeriod, ageGroup, gender, isYobRanking, overallRanking, endOfYearRanking);
    List<ListingItem> filteredItems = result.getListingItems().stream().filter(r -> r.club().contains(club)).toList();
    result.setListingItems(filteredItems);

    return result;
  }

  /**
   * retrieve the age group rankings from the datastore and map them to the json structure for the clients.
   *
   * @param rankingPeriod ranking period
   * @param ageGroup age group
   * @param gender gender
   * @param isYobRanking requested yob ranking only
   * @param overallRanking requested overall ranking including younger players
   * @param endOfYearRanking requested end of year ranking
   * @return listing container
   */
  public Listing getAgeGroupRankings(LocalDate rankingPeriod, String ageGroup, String gender, boolean isYobRanking, boolean overallRanking, boolean endOfYearRanking) {
    String ageGroupWithGenderMarker = genderShortForm(gender) + ageGroup.toUpperCase();
    Listing result = Listing.builder()
        .rankingPeriod(rankingPeriod)
        .ageGroup(ageGroupWithGenderMarker)
        .yobRanking(isYobRanking)
        .overallRanking(overallRanking)
        .endOfYearRanking(endOfYearRanking)
        .build();
    List<Ranking> rankingsfromDb = rankingRepository.getRankingsForListing(rankingPeriod, ageGroup, gender, isYobRanking, overallRanking, endOfYearRanking);
    List<ListingItem> listingItems = new ArrayList<>();
    for (Ranking r : rankingsfromDb) {
      ListingItem item = new ListingItem(r.position(), r.dtbId(), r.firstname(), r.lastname(), r.nationality(), r.club(), r.federation(), r.points());
      listingItems.add(item);
    }
    result.setListingItems(listingItems);
    return result;
  }

  private String genderShortForm(String gender) {
    if ("boys".equalsIgnoreCase(gender)) {
      return "m";
    } else {
      return "w";
    }
  }
}
