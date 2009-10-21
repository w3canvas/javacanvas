/**
 *
 */
package com.w3canvas.javacanvas.rt;

import java.awt.EventQueue;

import org.mozilla.javascript.Context;

class RhinoScheduler implements Runnable
{

    private RhinoScriptRunner runner;
    private int time;
    private int run;
    private boolean loop;

    RhinoScheduler(RhinoRuntime runtime, Object command, int time, boolean loop)
    {
        this.time = time;
        this.run = 0;
        this.loop = loop;

        runner = new RhinoScriptRunner(runtime, command);
    }

    public void run()
    {
        do
        {
            try
            {
                Thread.sleep(time);
            }
            catch (InterruptedException e)
            {
                // Auto-generated catch block
                throw new RuntimeException(e);
            }

            if (run != 0)
            {
                break;
            }

            if (EventQueue.isDispatchThread())
            {
                runner.run(Context.enter());
            }
            else
            {
                try
                {
                    EventQueue.invokeAndWait(runner);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        } while (loop);
    }

    public void stopLoop()
    {
        this.loop = false;
    }
}