package simulation.client

class HtmlExecutionTraceGenerator(val body : String ) {
    private fun makeHead(): String {
        return """<!DOCTYPE html>
<html lang="en">

  <head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Serif:ital@0;1&display=swap" rel="stylesheet">

    <style>
      * {
        font-family: 'Noto Serif', serif;
      }
    </style>
  </head>

  <body style="white-space: normal;">"""
    }

    private fun makeBottom() : String {
        return """        
  </body>

</html>"""
    }

    fun generate() : String  {
        return makeHead() + body + makeBottom()
    }
}
