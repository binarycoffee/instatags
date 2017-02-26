package mangoes.instatags;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity
{
    /**
     * Absolute path of post image (current)
     */
    private String imageAbsolutePath = null;

    /**
     * Absolute path of post image (new)
     */
    private String imageAbsolutePathNew = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Resolve image file path
     */
    private File resolveImageFilePath() throws IOException
    {
        // Holds timestamp for file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // Holds filename
        String filename = "IMAGE_" + timeStamp;

        // Holds empty image file
        File empty = null;

        // Holds storage directory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Create empty file using new filepath
        empty = File.createTempFile(filename, ".jpg", storageDir);

        // Set absolute post image path
        this.imageAbsolutePathNew = empty.getAbsolutePath();

        // Return image
        return empty;
    }

    /**
     * Take image for post
     */
    public void takePicture(View view)
    {

        // Holds image file
        File imageFile = null;

        // Holds uri file
        Uri imageURI = null;

        // Create intent to call camera
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Attempt to resolve activity for intent
        if(imageIntent.resolveActivity(getPackageManager()) != null)
        {
            // Grab image file
            try
            {
                // Resolve image path
                imageFile = this.resolveImageFilePath();

                // image uri
                Log.d("TEST", "over here");
                imageURI = FileProvider.getUriForFile(this, "mangoes.instatags.ImageStore", imageFile);
                Log.d("TEST", "here");

                // Set image capturing intent uri resource
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);

                // Start image taking activity
                startActivityForResult(imageIntent,1);
            }
            catch(IOException e)
            {
            }
        }
    }

    public void uploadPicture(View view)
    {

        // Holds image file
        File imageFile = null;

        // Holds uri file
        Uri imageURI = null;

        // Create intent to call gallery
        Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageIntent.setType("image/*");

        //Choosing location of image pick
        Intent choiceIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        choiceIntent.setType("image/*");

        //Choosing the image
        Intent pickIntent = new Intent(Intent.createChooser(imageIntent, "Select Image"));
        pickIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {choiceIntent});

        startActivityForResult(pickIntent, 1);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("CHEASMAE", "result code: " + resultCode);
        if (resultCode == RESULT_OK){
            switch(requestCode)
            {
                case 1:

                            this.handleImage();
                    break;

                case 2:
                    Log.d("CHEASMAE_LOCATION","activity result");
                    break;
            }
        }

    }

    /**
     * Handle post media photo
     */
    private void handleImage()
    {

        // Create bitmap to contain
        ImageView takenPicture = (ImageView)findViewById(R.id.pictureTaken);

        Bitmap bitmap = (BitmapFactory.decodeFile(this.imageAbsolutePathNew));

        takenPicture.setImageBitmap(bitmap);
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                final ClarifaiClient client = new ClarifaiBuilder("jaxaSxQtMpzIoodkJLLh2Xjtie0G-13VtRxMY-td", "pktFNejsWh11mlELBUrnRuoty6Zh5bXSyGrX1R6d").buildSync();
                ClarifaiResponse<List<ClarifaiOutput<Concept>>> res = client.getDefaultModels()
                    .generalModel().predict().withInputs
                            (
                                    ClarifaiInput.forImage
                                            (
                                                    ClarifaiImage.of
                                                            (
                                                                    new File(imageAbsolutePathNew)
                                                            )
                                            )
                            ).executeSync();

                ArrayList<String> tags = new ArrayList<>();
               //Iterate through ClarifaiResponse name attributes a.k.a. tags
                for(int i = 0; i < res.get().get(0).data().size(); i++){
                    tags.add(res.get().get(0).data().get(i).name().toString());
                }

            }
        }
        ).start();



    }


}
