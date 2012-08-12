$ = jQuery

$.fn.extend
  tweetArea: (options) -> 
    settings = 
      offset: 21
      charDisplay: '#char-count'
      warnAt: 120
      postBox: '#tweet-area'
      max: 140
      warnClass: 'label-important'
      noWarnClass: 'label-info'

    settings = $.extend settings, options
    
    checkPostForURL = (post) -> 
      matches = []
      # regex taken from https://gist.github.com/1033143
      urlexp = /\b((?:https?:\/\/|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))/gi
      
      if post and urlexp.test(post)
        offset = 0
        matches = post.match urlexp

        $.each matches, () ->
          len = this.length
          matchoffset = len - settings.offset
          offset = offset + matchoffset

        offset

    pbId = settings.postBox.replace /^#/,''
    cntId = settings.charDisplay.replace /^#/, ''
    elemHtml = '<textarea id="'+pbId+'" name="'+pbId+'"></textarea><br />'
    elemHtml += 'There are <span id="'+cntId+'" class="label">140</span> characters remaining.'

    return @each () ->
      $this = $(this)
      $this.html elemHtml

      $textBox = $(settings.postBox)
      $disp = $(settings.charDisplay)
      
      handler = (e) ->
        a = $textBox.val().length
        post = $textBox.val()

        offset = checkPostForURL post
        a = a - offset if offset
        $disp.text(settings.max-a)
        if a > settings.warnAt
          if settings.warnClass and not $disp.hasClass(settings.warnClass)
            $disp.removeClass settings.noWarnClass
            $disp.addClass settings.warnClass
        else
          if settings.noWarnClass and not $disp.hasClass(settings.noWarnClass)
            $disp.removeClass settings.warnClass
            $disp.addClass settings.noWarnClass

      $textBox.keyup handler
      $this
      
$ ->
  $('#tweetArea').tweetArea()
​