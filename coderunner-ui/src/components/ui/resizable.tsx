import * as ResizablePrimitive from "react-resizable-panels"

import { cn } from "@/lib/utils"

function ResizablePanelGroup({
  className,
  orientation = "horizontal",
  ...props
}: ResizablePrimitive.GroupProps) {
  return (
    <ResizablePrimitive.Group
      data-slot="resizable-panel-group"
      orientation={orientation}
      className={cn(
        "flex h-full w-full",
        orientation === "vertical" && "flex-col",
        className
      )}
      {...props}
    />
  )
}

function ResizablePanel({ ...props }: ResizablePrimitive.PanelProps) {
  return <ResizablePrimitive.Panel data-slot="resizable-panel" {...props} />
}

interface ResizableHandleProps extends ResizablePrimitive.SeparatorProps {
  withHandle?: boolean
  orientation?: "horizontal" | "vertical"
}

function ResizableHandle({
  withHandle,
  className,
  orientation = "horizontal",
  ...props
}: ResizableHandleProps) {
  const isVertical = orientation === "vertical"
  
  return (
    <ResizablePrimitive.Separator
      data-slot="resizable-handle"
      className={cn(
        "relative flex items-center justify-center bg-zinc-800 transition-colors hover:bg-zinc-600",
        "focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none",
        isVertical 
          ? "h-2 w-full cursor-row-resize" 
          : "h-full w-px cursor-col-resize",
        className
      )}
      {...props}
    >
      {withHandle && (
        <div 
          className={cn(
            "z-10 rounded-full bg-zinc-500 transition-colors",
            isVertical 
              ? "h-1 w-12" 
              : "h-12 w-1"
          )} 
        />
      )}
    </ResizablePrimitive.Separator>
  )
}

export { ResizableHandle, ResizablePanel, ResizablePanelGroup }
