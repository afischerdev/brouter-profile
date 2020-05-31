/**
 * Container for link between two Osm nodes
 *
 * @author ab
 */
package btools.router;

import btools.util.FastMath;

final class WaterwayPath extends OsmPath
{
  final boolean DEBUG = false;
  
  /**
   * The elevation-hysteresis-buffer (0-10 m)
   */
  private int ehbd; // in micrometer
  private int ehbu; // in micrometer

  private float totalTime;  // travel time (seconds)
  private float totalEnergy; // total route energy (Joule)
  private float elevation_buffer; // just another elevation buffer (for travel time)

  private double extraTime;  // extra travel time (seconds)

  // Gravitational constant, g
  private static final double GRAVITY = 9.81;  // in meters per second^(-2)

  @Override
  public void init( OsmPath orig )
  {
    WaterwayPath origin = (WaterwayPath)orig;
    this.ehbd = origin.ehbd;
    this.ehbu = origin.ehbu;
    this.totalTime = origin.totalTime;
    this.totalEnergy = origin.totalEnergy;
    this.elevation_buffer = origin.elevation_buffer;
  }

  @Override
  protected void resetState()
  {
    ehbd = 0;
    ehbu = 0;
    totalTime = 0.f;
    totalEnergy = 0.f;
    elevation_buffer = 0.f;
  }

  @Override
  protected double processWaySection( RoutingContext rc, double distance, double delta_h, double elevation, double angle, double cosangle, boolean isStartpoint, int nsection, int lastpriorityclassifier )
  {
	 
	WaterwayModel wm = (WaterwayModel)rc.pm;
 
    // calculate the costfactor inputs
    float turncostbase = rc.expctxWay.getTurncost();
    float costfactor = rc.expctxWay.getCostfactor();
    float initialcost = rc.expctxWay.getInitialcost();

    int dist = (int)distance; // legacy arithmetics needs int

	if (extraTime != initialcost) {
		extraTime += initialcost;
	}
	
	int controlDraft = (int) rc.expctxWay.getVariableValue( "control_draft", 0 );
	int controlWidth = (int) rc.expctxNode.getVariableValue( "control_bridge_width", 0 );
	//if (DEBUG) System.out.println("control draft " +  controlDraft);
	if (controlWidth == 1 && initialcost <= wm.bridgeWaitingTime) {
	  boolean canPass = checkHeightValue(rc, wm.boatWidth, wm.key_ww_clearance_width, 0f);
	  if (DEBUG) 
		  System.out.println("Bridge way width can pass " + canPass  );
	  if (!canPass) initialcost = 10000.f;
	}
	if (controlDraft == 1) {
	  boolean canPass = checkHeightValue(rc, wm.boatDraft, wm.keys_ww_depth, 100f, false);
	  if (DEBUG) {
		  System.out.println("draft " +  controlDraft + "can pass " + canPass );
	  }
	  if (canPass) initialcost = 0f; else initialcost = 10000f;
	}

    // penalty for turning angle
    int turncost = (int)((1.-cosangle) * turncostbase + 0.2 ); // e.g. turncost=90 -> 90 degree = 90m penalty
    if ( message != null )
    {
      message.linkturncost += turncost;
      message.turnangle = (float)angle;
    }

    double sectionCost = turncost;

 
    // get the effective costfactor (slope dependent)
 
    if ( message != null )
    {
      message.costfactor = costfactor;
    }

	if (DEBUG)  System.out.println("WaterwayPath: dist:" + dist + " costfactor:" + costfactor + " sectionCost:" + (dist * costfactor + 0.5f) + " initialcost:" + initialcost + " extraTime:" + extraTime);
	
    sectionCost += (dist * costfactor + 0.5f); // + initialcost;

    return sectionCost;
  }

  @Override
  protected double processTargetNode( RoutingContext rc )
  {
  	WaterwayModel wm = (WaterwayModel)rc.pm;

    // finally add node-costs for target node
    if ( targetNode.nodeDescription != null )
    {
      boolean nodeAccessGranted = rc.expctxWay.getNodeAccessGranted() != 0.;
      rc.expctxNode.evaluate( nodeAccessGranted , targetNode.nodeDescription );
      double initialcost = rc.expctxNode.getInitialcost();
	  
	  int controlFixedHeight = (int) rc.expctxNode.getVariableValue( "control_bridge_fixed_height", 0 );
	  int controlOpeningHeight = (int) rc.expctxNode.getVariableValue( "control_bridge_opening_height", 0 );
	  int controlWidth = (int) rc.expctxNode.getVariableValue( "control_node_bridge_width", 0 );
	  int controlDraft = (int) rc.expctxNode.getVariableValue( "control_node_draft", 0 );
	  if (DEBUG) System.out.println("Bridge fix " +  controlFixedHeight + " open " + controlOpeningHeight);
	  if (controlOpeningHeight == 1) {
		  boolean canPass = checkHeightValue(rc, wm.boatHeight, wm.key_clearance_height_closed, 0f);
		  if (DEBUG) System.out.println("Bridge open can pass " + canPass  );
		  if (canPass) initialcost = 0f; else initialcost = wm.bridgeWaitingTime;
		  canPass = checkHeightValue(rc, wm.boatHeight, wm.key_clearance_height_open, 100f);
		  if (!canPass) initialcost = 10000f;
	  }
	  if (controlFixedHeight == 1 ) {
		  boolean canPass = checkHeightValue(rc, wm.boatHeight, wm.key_clearance_height_safe, 0f);
		  if (!canPass) canPass = checkHeightValue(rc, wm.boatHeight, wm.key_clearance_height, 0f);
		  if (DEBUG) System.out.println("Bridge fixed can pass " + canPass  );
		  if (canPass) initialcost = 0f;  else initialcost = 10000.f;
	  }
	  if (controlWidth == 1 && initialcost <= wm.bridgeWaitingTime) {
		  boolean canPass = checkHeightValue(rc, wm.boatWidth, wm.key_clearance_width, 0f);
		  if (DEBUG) 
			  System.out.println("Bridge width can pass " + canPass  );
		  if (!canPass) initialcost = 10000.f;
	  }
	  if (controlDraft == 1 ) {
		  boolean canPass = checkHeightValue(rc, wm.boatDraft, wm.keys_depth, 100f, true);
		  if (DEBUG) System.out.println("draft can pass " + canPass  );
		  if (!canPass) initialcost = 10000.f;
	  }
	  
      if ( initialcost >= 1000000. )
      {
        return -1.;
      }
      if ( message != null )
      {
        message.linknodecost += (int)initialcost;
        message.nodeKeyValues = rc.expctxNode.getKeyValueDescription( nodeAccessGranted, targetNode.nodeDescription );
      }
	  // do not double
	  if (extraTime != initialcost) {
	    if (DEBUG)  System.out.println("WaterwayPath: processTargetNode initialcost:" + initialcost + " extraTime:" + extraTime);
		extraTime = initialcost;
		totalTime += initialcost;
	  }
      return initialcost;
    }
	if (DEBUG)  System.out.println("WaterwayPath: processTargetNode initialcost:0");
    return 0.;
  }
  
  private boolean checkHeightValue(RoutingContext rc, float boat, int key, float start) {
	float value = start;
	if (rc.expctxNode != null) {
		try {
			float tmp = rc.expctxNode.getLookupValue( true, targetNode.nodeDescription, key );
			if (tmp != -1f) value = tmp;
		} catch(Exception e) {System.out.println("WaterwayPath: " + key + " e:" + e.getMessage()); }
	}

	if (DEBUG) 
		System.out.println("height " + value + " boat " + boat + " default " + start + " can pass " + (boat < value));
	if (boat < value) return true;
	else return false;
  }

  private boolean checkHeightValue(RoutingContext rc, float boat, int[] keys, float start, boolean bnode) {
	float value = start;
	int i = 0;
	if ( bnode && rc.expctxNode != null ||
	     rc.expctxWay != null ) {
		try {
			for (i = 0; i < keys.length; i++) {
				if (keys[i] == -1) continue;
				float tmp = bnode ? 
							rc.expctxNode.getLookupValue( true, targetNode.nodeDescription, keys[i] ) :
							rc.expctxWay.getLookupValue( true, link.descriptionBitmap, keys[i] );
				if (tmp != -1f) { 
					value = tmp;
					break;
				}
			}
		} catch(Exception e) {System.out.println("WaterwayPath: keys " + keys[i] + " e:" + e.getMessage()); }
	}

	if (DEBUG) 
		System.out.println("start " + (bnode?"Node ":"Way ") + i + " value " + value + " boat " + boat + " default " + start + " can pass " + (boat < value));
	if (boat < value) return true;
	else return false;
  }

  @Override
  public int elevationCorrection( RoutingContext rc )
  {
	if (DEBUG)  System.out.println("WaterwayPath: elevationCorrection");
    return ( rc.downhillcostdiv > 0 ? ehbd/rc.downhillcostdiv : 0 )
         + ( rc.uphillcostdiv > 0 ? ehbu/rc.uphillcostdiv : 0 );
  }

  @Override
  public boolean definitlyWorseThan( OsmPath path, RoutingContext rc )
  {
	if (DEBUG)  System.out.println("WaterwayPath: definitlyWorseThan");
    WaterwayPath p = (WaterwayPath)path;

	  int c = p.cost;
	  if ( rc.downhillcostdiv > 0 )
	  {
	    int delta = p.ehbd - ehbd;
	    if ( delta > 0 ) c += delta/rc.downhillcostdiv;
	  }
	  if ( rc.uphillcostdiv > 0 )
	  {
	    int delta = p.ehbu - ehbu;
	    if ( delta > 0 ) c += delta/rc.uphillcostdiv;
	  }

	  return cost > c;
  }


  @Override
  protected void computeKinematic( RoutingContext rc, double dist, double delta_h, boolean detailMode )
  {
	
    if ( !detailMode )
    {
      return;
    }
	
  	WaterwayModel wm = (WaterwayModel)rc.pm;

    double wayMaxspeed;
   
    wayMaxspeed = rc.expctxWay.getMaxspeed() / 3.6f;
    if (wayMaxspeed == 0)
    {
        wayMaxspeed = rc.maxSpeed;
    }
    wayMaxspeed = Math.min(wayMaxspeed,rc.maxSpeed);
    
    double speed = wayMaxspeed; // Travel speed
	
    float dt = (float) ( dist / speed );
	if (DEBUG)  System.out.println("WaterwayPath: computeKinematic extraTime:" + extraTime);
    totalTime += dt + extraTime;
	
    // Calc energy assuming biking (no good model yet for hiking)
    // (Count only positive, negative would mean breaking to enforce maxspeed)
    double energy = dt * 1000.; //dist; //*(rc.S_C_x*speed*speed + f_roll);
    if ( energy > 0. )
    {
      totalEnergy += energy * wm.literHour;
    }
  }

  @Override
  public double getTotalTime()
  {
    return totalTime;
  }

  @Override
  public double getTotalEnergy()
  {
    return totalEnergy;
  }
}
