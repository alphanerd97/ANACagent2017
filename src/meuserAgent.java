import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.EndNegotiation;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.parties.NegotiationParty;
import negotiator.persistent.PersistentDataContainer;
import negotiator.protocol.MultilateralProtocol;
import negotiator.protocol.StackedAlternatingOffersProtocol;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

/*
 * @author Paul Meuser
 * Multilateral negotiating agent for 2017 ANAC competition
 * Version 1.0
 */

public class meuserAgent extends AbstractNegotiationParty{
	private SortedOutcomeSpace bidPool;
	private Action mostRecentAction = null;
	private Bid mostRecentBid = null;
	private Bid nextBid = null;
	private double maxUtil;
	
	
	
	public meuserAgent() {
		super();
	}
	
	@Override
	public  void  init(NegotiationInfo info) {
		
		super.init(info);
		
		//determine available bidding space, set next bid with max utility and record max utility
		bidPool = new SortedOutcomeSpace(utilitySpace);
		nextBid = bidPool.getMaxBidPossible().getBid();
		maxUtil = getUtility(nextBid);
		
		mostRecentAction = null;
		
	}
	
	@Override
	public Action chooseAction(List <Class <? extends Action >> possibleActions ) {
		Action output;
		//for first bid, present bid with maximum possible utility.
		if (mostRecentAction == null) {
			output = new Offer(getPartyId(),nextBid);
		}
		else if (mostRecentAction instanceof Offer) {
			updateNextBid();
			//accept bids which exceed expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)) {
				output = new Accept(getPartyId(),mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getPartyId(),nextBid);
			}
		}
		else if (mostRecentAction instanceof Accept) {
			updateNextBid();
			//accept bids which exceed half expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)*2/3) {
				output = new Accept(getPartyId(),mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getPartyId(),nextBid);
			}
		}
		else {
			updateNextBid();
			output =  new Offer(getPartyId(),nextBid);
		}
		
		return output;
	}
	
	//Determines next bid by conceding utility at increasing rate as deadline approaches.
	private void updateNextBid() {
		//find the current time as fraction of total time available
		double now = getTimeLine().getTime();
		//generate next expected utility based on current time
		double util = maxUtil * (1.0-Math.exp(5.0*(now-1.0)));
		//generate bid using expected utility
		nextBid = bidPool.getBidNearUtility(util).getBid();
	}
	
	
	@Override
	public void receiveMessage(AgentID sender , Action action ) {
		super.receiveMessage(sender, action);
		//collect most recent action, and bid
		mostRecentAction = action;
		if (mostRecentAction instanceof Offer) {
			mostRecentBid = ((Offer) mostRecentAction).getBid();
		}
		if (mostRecentAction instanceof Accept) {
			mostRecentBid = ((Accept) mostRecentAction).getBid();			
		}
	}
	
	public String getDescription () {
		return "Paul Meuser ANAC Agent";
	}
	
	public Class <? extends MultilateralProtocol > getProtocol () {
		return (new StackedAlternatingOffersProtocol()).getClass();
	}
	
	public HashMap <String , String > negotiationEnded(Bid acceptedBid ) {
		return null;
	}

	
	
}
