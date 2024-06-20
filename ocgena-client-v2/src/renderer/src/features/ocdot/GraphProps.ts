export interface GraphProps {
    dotSrc : string
    transitionDuration?: number
    fit : boolean
    
    registerParentSizeUpdate: (onParentSizeUpdate: () => void) => void;
}