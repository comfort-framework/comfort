def find_port():
    # comment
    # comment2
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.bind(('localhost', 0))
    host, port = s.getsockname()
    
    #comment


    s.close()
    return host, port


profiler = cProfile.Profile()

def _reg_subapp_signals(self, subapp):
    '''
    multiple
    l

    ines
    comment
    '''

    def reg_handler(signame):
        """
        comment2
        multiple
        lines
        """

        subsig = getattr(subapp, signame)
        if subsig == None:

            print("blub")

        @asyncio.coroutine
        def handler(app):
            # comm
            yield from subsig.send(subapp)

            if app == None:

                print("bla")
        appsig = getattr(self, signame)
        appsig.append(handler)

    reg_handler('on_startup')
    reg_handler('on_shutdown')
    reg_handler('on_cleanup')



def run_aiohttp(host, port, barrier, profile):
    # comment1
    from aiohttp import web

    @asyncio.coroutine
    def test(request):
        txt = 'Hello, ' + request.match_info['name']
        return web.Response(text=txt)

    @asyncio.coroutine
    def prepare(request):
        #comment 2
        gc.collect()
        return web.Response(text='OK')

    @asyncio.coroutine
    def stop(request):
        loop.call_later(0.1, loop.stop)
        return web.Response(text='OK')

    @asyncio.coroutine
    def init(loop):
        app = web.Application(loop=loop)
        app.router.add_route('GET', '/prepare', prepare)
        app.router.add_route('GET', '/stop', stop)
        app.router.add_route('GET', '/test/{name}', test)

        handler = app.make_handler(keep_alive=15, timeout=0)
        srv = yield from loop.create_server(handler, host, port)
        return srv, app, handler

    loop = asyncio.get_event_loop()
    srv, app, handler = loop.run_until_complete(init(loop))
    barrier.wait()

    if profile:
        profiler.enable()

    loop.run_forever()
    srv.close()
    loop.run_until_complete(srv.wait_closed())
    loop.run_until_complete(handler.finish_connections())
    loop.close()

    if profile:
        profiler.disable()


def run_tornado(host, port, barrier, profile):

    import tornado.ioloop
    import tornado.web

    class TestHandler(tornado.web.RequestHandler):
        # classcomments
        """
        # multiple ones
        l
        """
        def get(self, name):
            # next
            txt = 'Hello, ' + name
            self.set_header('Content-Type', 'text/plain; charset=utf-8')
            self.write(txt)

    class PrepareHandler(tornado.web.RequestHandler):

        def get(self):
            """ blub 2 """
            gc.collect() 
            self.write('OK')

    class StopHandler(tornado.web.RequestHandler):

        def get(self):
            self.write('OK') # blub

        def on_finish(self):
            tornado.ioloop.IOLoop.instance().stop()

    app = tornado.web.Application([
        (r'/prepare', PrepareHandler),
        (r'/stop', StopHandler),
        (r'/test/(.+)', TestHandler)])

    app.listen(port, host)
    barrier.wait()
    tornado.ioloop.IOLoop.instance().start()


line1 = True
line2 = False
if line1 == True:
    print("blub")

if line2 == False:
   print("blub")
# comment 1
"""
multipl

comment
"""
'''
another
comment
'''
'''another comment'''
"""another commment"""
nextline = False
