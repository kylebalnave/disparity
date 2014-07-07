Disparity
=========

[1]: https://github.com/kylebalnave/semblance           "Semblance"
[2]: https://github.com/kylebalnave/snaphot             "Snapshot"
[3]: https://github.com/BBC-News/wraith                 "BBC-News Wraith"

A command line tool and [Semblance][1] Runner that compares images and creates a difference image.
When used in conjunction with [Snapshot][2], it can be used as an alternative to [BBC-News Wraith][3]

### Commandline Usage

    java -jar dist/Disparity.jar -config ./config.json
    
### Dependencies

The Semblance.jar [Semblance][1] should be included in the classpaths.

### Example Config

The below configuration will compare files in the directory ./screenshots/

    {
        "threads": 5,
        "in": "./screenshots/",
        "out": "./screenshots/",
        "fuzzyness": 10,
        "reports": [
            {
                "className": "semblance.reporters.JunitReport",
                "out": "./reports/disparity.junit"
            }
        ]
    }	

### Config Explanation	

- threads

Number of parallel threads to use.

- in

The folder to check for images.
Each folder should be named yyyy-MM-dd HH-mm-SS and match the following pattern ^\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}-\\d{2}-\\d{2}$.  All other folders will be ignored.

- out

The folder to create the diff folder in for all differenced images.

- fuzzyness

The tolerance between RGB values allowed.  This is to allow JPEGs to be used without throwing failures.

- reports

Report details for all results
