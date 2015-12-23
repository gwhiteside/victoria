package net.georgewhiteside.victoria;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.AckValue;
import com.ebay.services.finding.FindCompletedItemsRequest;
import com.ebay.services.finding.FindCompletedItemsResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.services.finding.ItemFilter;
import com.ebay.services.finding.ItemFilterType;
import com.ebay.services.finding.PaginationInput;
import com.ebay.services.finding.PaginationOutput;
import com.ebay.services.finding.SearchItem;
import com.ebay.services.finding.SortOrderType;

public class EbayMiner {
	
	private String ebayAppId;
	
	private ClientConfig clientConfig;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public EbayMiner(String ebayAppId) {
		this.ebayAppId = ebayAppId;
		clientConfig = new ClientConfig();
		clientConfig.setApplicationId(ebayAppId);
		clientConfig.setSoapMessageLoggingEnabled(false);
		clientConfig.setHttpHeaderLoggingEnabled(false);
	}
	
	/**
	 * 
	 * @param searchString eBay search keywords
	 * @param startDate Unix timestamp (seconds since January 1st, 1970), inclusive
	 * @param endDate Unix timestamp (seconds since January 1st, 1970), exclusive
	 * @return
	 */
	public List<SearchItem> getSales(String searchString, long startDate, long endDate) {
		FindingServicePortType serviceClient = FindingServiceClientFactory.getServiceClient(clientConfig);
		
		FindCompletedItemsRequest request = new FindCompletedItemsRequest();
		
		request.setKeywords(searchString);
		request.setSortOrder(SortOrderType.END_TIME_SOONEST);
		request.getCategoryId().add(EbayUtils.EBAY_CAT_VIDEO_GAMES);
		request.getItemFilter().add(Filter.soldItemsOnly());
		request.getItemFilter().add(Filter.endTimeFrom(startDate));
		request.getItemFilter().add(Filter.endTimeTo(endDate));
		
		List<SearchItem> searchResults = new ArrayList<SearchItem>();

		int i = 0;
		int totalPages, pageNumber;
		
		do {
			i++;
			request.setPaginationInput(page(i));
			
			FindCompletedItemsResponse response = serviceClient.findCompletedItems(request);
			
			logResponseAck(response.getAck());
			
			searchResults.addAll(response.getSearchResult().getItem());
			
			PaginationOutput pagination = response.getPaginationOutput();
			totalPages = pagination.getTotalPages();
			pageNumber = pagination.getPageNumber();
			
			logResponsePagination(pagination);
			
		} while(pageNumber < totalPages);
		
		return searchResults;
	}
	
	private void logResponsePagination(PaginationOutput pagination) {
		if(pagination.getPageNumber() == 1) {
			log.debug("{} search results", pagination.getTotalEntries());
		}
		log.debug("Retrieved page {} of {}", pagination.getPageNumber(), pagination.getTotalPages());
	}
	
	private void logResponseAck(AckValue ack) {
		String responseString = "eBay API response: {}";;
		switch(ack) {
		case SUCCESS:
			// no problem
			break;
		case FAILURE:
			log.error(responseString, ack);
			break;
		case PARTIAL_FAILURE: // intentional fall-through
		case WARNING:
			log.warn(responseString, ack);
			break;
		default:
			log.warn(responseString, ack);
			break;
		}
	}
	
	private static PaginationInput page(int pageNumber) {
		PaginationInput pi = new PaginationInput();
		pi.setEntriesPerPage(100);
		pi.setPageNumber(pageNumber);
		return pi;
	}
	
	private static class Filter {
		private static ItemFilter soldItemsOnly() {
			return filter(ItemFilterType.SOLD_ITEMS_ONLY, "true");
		}
		
		private static ItemFilter endTimeFrom(long unixTime) {
			return filter(ItemFilterType.END_TIME_FROM, EbayUtils.unixTimeISO8601(unixTime));
		}
		
		private static ItemFilter endTimeTo(long unixTime) {
			return filter(ItemFilterType.END_TIME_TO, EbayUtils.unixTimeISO8601Exclusive(unixTime));
		}
		
		private static ItemFilter filter(ItemFilterType type, String value) {
			ItemFilter filter = new ItemFilter();
			filter.setName(type);
			filter.getValue().add(value);
			return filter;
		}
	}
}
