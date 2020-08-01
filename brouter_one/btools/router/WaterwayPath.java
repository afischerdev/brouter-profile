/**
 * path rules for waterway
 *
 * @author axel
 */
package btools.router;

import btools.util.FastMath;

final class WaterwayPath extends OsmPath
{
  final boolean DEBUG = false;
  
  final float DO_NOT_USE = 100000f;
	
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
    this.totalTime = origin.totalTime;
    this.totalEnergy = origin.totalEnergy;
    this.elevation_buffer = origin.elevation_buffer;
  }

  @Override
  protected void resetState()
  {
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
	  
	  
      if ( initialcost >= DO_NOT_USE )
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
      return (wm.shortestWay == 0 || initialcost >= DO_NOT_USE )? initialcost :  0d;
    }
	if (DEBUG)  System.out.println("WaterwayPath: processTargetNode initialcost:0");
    return 0.;
  }
  

  @Override
  public int elevationCorrection( RoutingContext rc )
  {
	if (DEBUG)  System.out.println("WaterwayPath: elevationCorrection");
    return 0;
  }

  @Override
  public boolean definitlyWorseThan( OsmPath path, RoutingContext rc )
  {
	if (DEBUG)  System.out.println("WaterwayPath: definitlyWorseThan");
	return false;
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
    totalTime += dt + extraTime;
	if (DEBUG) 
		System.out.println("WaterwayPath: computeKinematic " + totalTime + " extraTime:" + extraTime);
	
    // Calc energy assuming biking (no good model yet for hiking)
    // (Count only positive, negative would mean breaking to enforce maxspeed)
    double energy = dt * 1000.; //dist; //*(rc.S_C_x*speed*speed + f_roll);
    if ( energy > 0. )
    {
      totalEnergy =  totalTime * 1000.f * wm.literHour;
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
