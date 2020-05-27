/**
 * Container for link between two Osm nodes
 *
 * @author ab
 */
package btools.router;

import java.util.Map;

import btools.expressions.BExpressionContext;
import btools.expressions.BExpressionContextNode;
import btools.expressions.BExpressionContextWay;


final class WaterwayModel extends OsmPathModel
{
  final boolean DEBUG = true;
	
  public OsmPrePath createPrePath()
  {
    return null;
  }

  public OsmPath createPath()
  {
    return new WaterwayPath();
  }

  protected BExpressionContextWay ctxWay;
  protected BExpressionContextNode ctxNode;
  protected Map<String,String> params;

  public  float boatHeight;
  public  float boatWidth;
  public  float boatDraft;
  public  int bridgeWaitingTime;
  public  int lockWaitingTime;

  public  int key_clearance_height_closed;
  public  int key_clearance_height_open;
  public  int key_clearance_height;
  public  int key_clearance_height_safe;
  public  int key_vertical_clearance_safe;
  public  int key_clearance_width;
  public  int key_ww_draft;
  public  int key_maxdraft;
  public  int key_ww_maxdraft;
  public  int key_ww_depth;
  public  int key_depth;
  public  int key_waterdepth;
  public  int[] keys_depth;
  
  @Override
  public void init( BExpressionContextWay expctxWay, BExpressionContextNode expctxNode, Map<String,String> keyValues )
  {
	if (DEBUG) System.out.println("WaterwayModel: init");
	
    ctxWay = expctxWay;
    ctxNode = expctxNode;
  
    BExpressionContext expctxGlobal = expctxWay; // just one of them...
	
	params = keyValues;
 
	// global values
    boatHeight = getParam( "boat_height", 1f );
    boatWidth = getParam( "boat_width", 1f );
    boatDraft = getParam( "boat_draft", 1f );
    bridgeWaitingTime = (int) getParam( "waiting_bridge", 0f );
    lockWaitingTime = (int) getParam( "waiting_lock", 0f );
	
	key_clearance_height_closed = expctxNode.getLookupKey("seamark:bridge:clearance_height_closed");
	key_clearance_height_open = expctxNode.getLookupKey("seamark:bridge:clearance_height_open");
	key_clearance_height = expctxNode.getLookupKey("seamark:bridge:clearance_height");
	key_clearance_height_safe = expctxNode.getLookupKey("seamark:bridge:clearance_height_safe");
	key_vertical_clearance_safe = expctxNode.getLookupKey("seamark:cable_overhead:vertical_clearance_safe");
	key_clearance_width = expctxNode.getLookupKey("seamark:bridge:clearance_width");
	key_ww_draft = expctxWay.getLookupKey("draft");
	key_ww_maxdraft = expctxWay.getLookupKey("maxdraft");
	key_ww_depth = expctxWay.getLookupKey("depth");
	key_maxdraft = expctxNode.getLookupKey("maxdraft");
	key_depth = expctxNode.getLookupKey("depth");
	key_waterdepth = expctxNode.getLookupKey("water:depth");
	if (DEBUG) {
		System.out.println( "key_clearance_height_closed " + key_clearance_height_closed);
		System.out.println( "key_clearance_height_open " + key_clearance_height_open);
	    System.out.println( "key_clearance_height " + key_clearance_height);
	    System.out.println( "key_clearance_height_safe " + key_clearance_height_safe);
	    System.out.println( "key_vertical_clearance_safe " + key_vertical_clearance_safe);
	    System.out.println( "key_clearance_width " + key_clearance_width);
	    System.out.println( "key_ww_draft " + key_ww_draft);
	    System.out.println( "key_ww_maxdraft " + key_ww_maxdraft);
	    System.out.println( "key_ww_depth " + key_ww_depth);
	    System.out.println( "key_maxdraft " + key_maxdraft);
	    System.out.println( "key_depth " + key_depth);
	    System.out.println( "key_waterdepth " + key_waterdepth);
	}
  }
  
  protected float getParam( String name, float defaultValue )
  {
    String sval = params == null ? null : params.get( name );
    if ( sval != null )
    {
      return Float.parseFloat( sval );
    }
    float v = ctxWay.getVariableValue( name, defaultValue );
    if ( params != null )
    {
      params.put( name, "" + v );
    }
    return v;
  }

}
