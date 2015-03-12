package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriseActivity extends Activity {

	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private Task scopedTask; // l'activite sur laquelle se trouve le scope
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des activites
	private final int W; // largeur de la frise en px
	private final int H; // hauteur de la frise en px
	private boolean modeManuel = false; // mode manuel desactive au debut

	Button aide = null;
	Button menu = null;
	Button retour = null;
	Button manual = null;
	Button right = null;
	Button left = null;
	
	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		myTasks = new ArrayList<Task>();
		scopedTask = null;
		h0 = 8; // debut a 8h
		h1 = 21; // debut a 21h
		W = 820;
		H = 47;
		colorTab = new int[11];
		colorTab[0] = R.color.vert1; colorTab[1]=R.color.vert2;colorTab[2]=R.color.vert3;colorTab[3]=R.color.bleu1;
		colorTab[4]=R.color.bleu2;colorTab[5]=R.color.jaune1;colorTab[6]=R.color.orange1;colorTab[7]=R.color.orange2;
		colorTab[8]=R.color.orange3;colorTab[9]=R.color.rose;colorTab[10]=R.color.fushia;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frise);

		/* Changement de police du titre */
		TextView txtView1 = (TextView) findViewById(R.id.texte);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),"fonts/onthemove.ttf");
		txtView1.setTypeface(externalFont);

		/* Remplissage de mes taches par lecture du fichier */
		//Drawable image = getResources().getDrawable(R.drawable.image_dejeuner);
		//myTasks = TaskReader.read(this, "myTasks.txt");
		myTasks = Task.createTasks(this);

		/* Recuperation de la frise*/
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);

		int color_indice = 0;
		
		for (Task myTask : myTasks) {

			/* Affichage de ma tache sur la frise */
			ImageView rectTask = new ImageView(getApplicationContext());
			int Xwidth = myTask.getXwidth(W, h0, h1);

			/* Creation du rectangle et placement */
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Xwidth, H);
			layoutParams.setMargins(3, 3, 0, 0);
			rectTask.setLayoutParams(layoutParams);
			int couleur = getResources().getColor(colorTab[color_indice]);
			myTask.setCouleur(couleur); // on associe a la tache sa couleur
			rectTask.setBackgroundColor(couleur);
			frise.addView(rectTask);
			rectTask.setVisibility(View.VISIBLE);
			color_indice += 1;
		}
		
		/* Met le scope a l'activite en cours */
		//Task currentTask = findCurrentTask();
		Task currentTask = myTasks.get(4);
		if (!(currentTask == null)) {
			scopedTask = currentTask;
			replaceScope(); // place le scope sur la tahce
			displayTask(); // affiche les infos de la tache
		}
		
		/* creation des 3 boutons menu, aide et retour � l'activite precedente*/
		aide = (Button) findViewById(R.id.bouton_aide);
		menu = (Button) findViewById(R.id.bouton_menu);
		retour = (Button) findViewById(R.id.bouton_retour);
		
		menu.setOnClickListener(new View.OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  
	    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
	    	Drawable d = getResources().getDrawable(R.drawable.home_e);
	    	menu.setBackground(d);
	    	
	    	/* Passage a l'autre activite */
	        Intent secondeActivite = new Intent(FriseActivity.this,MenuActivity.class);
	        startActivity(secondeActivite);
	      }});
	      
	      aide.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  
		    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
		    	Drawable d = getResources().getDrawable(R.drawable.help_e);
		    	aide.setBackground(d);
		    	
		    	/* Passage a l'autre activite */
		        Intent secondeActivite = new Intent(FriseActivity.this,MenuAide.class);
		        startActivity(secondeActivite);
		      }});
		      
		      retour.setOnClickListener(new View.OnClickListener() {
			      @Override
			      public void onClick(View v) {
			    	  
			    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
			    	Drawable d = getResources().getDrawable(R.drawable.back_e);
			    	retour.setBackground(d);
			    	
			    	/* Passage a l'autre activite */
			        Intent secondeActivite = new Intent(FriseActivity.this,MenuActivity.class);
			        startActivity(secondeActivite);
			      }
	    });
		
		/* creation du mode manuel */      
		manual = (Button) findViewById(R.id.bouton_manual);
		left = (Button) findViewById(R.id.bouton_left);
		right = (Button) findViewById(R.id.bouton_right);
		Log.d("tag","scoped task = "+ Task.indexOfTask(myTasks, scopedTask));
		
		manual.setOnClickListener(new View.OnClickListener() {
			@Override
		      public void onClick(View v) {
				
				if(modeManuel){ // si on est en mode manuel
					
					manual.setBackground(getResources().getDrawable(R.drawable.manual)); // desenfonce le bouton
					modeManuel = false;
					left.setActivated(false); // desactivation des boutons
					right.setActivated(false);
					left.setVisibility(View.INVISIBLE); // disparition des boutons
					right.setVisibility(View.INVISIBLE);
				}
				else{ // si on n'est pas en mode manuel
					
				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
		    	Drawable d = getResources().getDrawable(R.drawable.manual_e);
		    	manual.setBackground(d);
		    	
		    	/* passage en mode manuel */
		    	modeManuel = true;
		    	left.setActivated(true); // activation des boutons
				right.setActivated(true);
				left.setVisibility(View.VISIBLE); // affichage des boutons fleches
				right.setVisibility(View.VISIBLE);
				}
				
			}});
		
		left.setOnClickListener(new View.OnClickListener() {
			@Override
		      public void onClick(View v) {
				
				moveScope(-1); // deplace le scope d'une activite vers l'arriere
				displayTask();
			}});
		
		right.setOnClickListener(new View.OnClickListener() {
			@Override
		      public void onClick(View v) {
				
				moveScope(1); // deplace le scope d'une activite vers l'avant
				displayTask();
				
			}});
		
		      
	}

	/**
	 * Trouve la tache qui se deroule a l'heure actuelle
	 * @return la tache actuelle
	 */
	Task findCurrentTask() {

		final Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		Calendar before = Calendar.getInstance();
		Calendar after = Calendar.getInstance();

		for (Task t : myTasks) { // parcours toutes les tasks

			double hDebut = t.getHeureDebut();
			int heureD = (int) Math.floor(hDebut);
			int minuteD = (int) ((hDebut - heureD) * 0.6);
			before.set(year, month, day, heureD, minuteD);

			double hFin = hDebut + t.getDuree();
			int heureF = (int) Math.floor(hFin);
			int minuteF = (int) ((hFin - heureF) * 0.6);
			after.set(year, month, day, heureF, minuteF);

			if ((now.compareTo(after) == -1) && (now.compareTo(before) == 1))
				return t; // si l'instant actuel est compris entre le debut et la fin de l'activite

		}
		return null;
	}
	
	/**
	 * Deplace le scope vers l'avant ou l'arriere
	 * @param pas le nombre relatif de pas en nombre d'activite a faire
	 */
	public void moveScope(final int pas){
		
		Task oldScopedTask = scopedTask;
		Task nextScopedTask = Task.findRelativeTask(myTasks,scopedTask,pas);
		this.scopedTask = nextScopedTask;
		ImageView scope = (ImageView) findViewById(R.id.scope);
		
		/* Creation de l'animation */
		
	    final int x1 = oldScopedTask.getXwidth(W,h0,h1);
	    final int x2 = nextScopedTask.getXwidth(W,h0,h1);

		// Translation
	    
		AnimationSet animationSet = new AnimationSet(true);
		int XDelta = nextScopedTask.getXbegin(W,h0, h1) - oldScopedTask.getXbegin(W,h0, h1);
		TranslateAnimation translate = new TranslateAnimation(0,(XDelta)*(x1/x2)+pas*(x1/x2)*3+7, 0, 0);
	    translate.setDuration(2000);
	    //translate.setStartOffset(1000);
	    animationSet.addAnimation(translate);
	    
	    // Mise a l'echelle
	    
	    double ratio =(double) x2/x1;
	    float ratioF = (float) ratio;		
	    ScaleAnimation scale = new ScaleAnimation(1,ratioF, 1, 1);
	    scale.setDuration(2000);
	    animationSet.addAnimation(scale);
	    
	    animationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
            	  replaceScope(); // replace vraiment le scope a sa nouvelle position
                }
            });

	    scope.startAnimation(animationSet);
	}
	
	/**
	 * Replace le scope a la position scopedTask sans animation
	 */
	public void replaceScope( ){
		
		ImageView scope = (ImageView) findViewById(R.id.scope);
		int indice = Task.indexOfTask(myTasks,scopedTask);
		int XBegin = scopedTask.getXbegin(W, h0, h1);
		int XWidth = scopedTask.getXwidth(W, h0, h1);
		MarginLayoutParams paramsScope = (MarginLayoutParams) scope.getLayoutParams();
		paramsScope.width = XWidth + 20;
		paramsScope.leftMargin = 290 + XBegin + indice*3;
		scope.setLayoutParams(paramsScope);
		
	}
	
    /**
     * Affiche les informations de la scopedTask au milieu de l'ecran
     */
	public void displayTask(){

		/* Recuperation du cadre et modification de sa couleur */
		ImageView cadre = (ImageView) findViewById(R.id.frame);
		int couleur = scopedTask.getCouleur(); // recuperation de la couleur
		cadre.setBackgroundColor(couleur);
		
		/* Affichage du titre de l'activite */
		TextView titreTask = (TextView) findViewById(R.id.titreTask);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),"fonts/onthemove.ttf");
		titreTask.setTypeface(externalFont);
		titreTask.setText(scopedTask.getNom());
		
		/* Affichage de l'image de l'activite */
		ImageView imageTask = (ImageView) findViewById(R.id.imageTask);
		imageTask.setBackground(scopedTask.getImage());
		
		/* Affiche l'heure de debut l'activite*/
		
	}
	
	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume(){
	    super.onResume();
	    final Button boutonRetour = (Button) findViewById(R.id.bouton_retour);
	    Drawable d1 = getResources().getDrawable(R.drawable.back);
    	boutonRetour.setBackground(d1);
    	 final Button boutonMenu = (Button) findViewById(R.id.bouton_menu);
 	    Drawable d2 = getResources().getDrawable(R.drawable.home);
     	boutonMenu.setBackground(d2);
     	 final Button boutonAide = (Button) findViewById(R.id.bouton_aide);
 	    Drawable d3 = getResources().getDrawable(R.drawable.help);
     	boutonAide.setBackground(d3);

	}
	
}
