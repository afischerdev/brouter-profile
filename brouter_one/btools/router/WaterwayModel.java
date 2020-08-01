/**
 * waterway model
 *
 * @author axel
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
  public  float literHour;
  
  public  int bridgeWaitingTime;
  public  int lockWaitingTime;
  public  int shortestWay;

  
  @Override
  public void init( BExpressionContextWay expctxWay, BExpressionContextNode expctxNode, Map<String,String> keyValues )
  {
	if (DEBUG) System.out.println("WaterwayModel: init " + keyValues	);
	
    ctxWay = expctxWay;
    ctxNode = expctxNode;
  
    BExpressionContext expctxGlobal = expctxWay; // just one of them...
	
	params = keyValues;
 
	// global values
    boatHeight = getParam( "boat_height", 1f );
    boatWidth = getParam( "boat_width", 1f );
    boatDraft = getParam( "boat_draft", 1f );
	literHour = getParam ( "literHour", 1f );
	
    bridgeWaitingTime = (int) getParam( "waiting_bridge", 0f );
    lockWaitingTime = (int) getParam( "waiting_lock", 0f );
    shortestWay = (int) getParam( "shortest_way", 0f );
	
	//setValues(expctxWay);
  }
  
  public void setValues(BExpressionContext ctx) {
	if (params != null) {
		for (Map.Entry<String, String> e : params.entrySet()) {
			//System.out.println("WaterwayModel: init " + e.getKey() + " " + e.getValue() );
			float f = Float.parseFloat(e.getValue());
			ctx.setVariableValue(e.getKey(), f, false );
		}
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
